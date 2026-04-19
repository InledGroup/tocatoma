import { Camera, CameraResultType } from '@capacitor/camera';
import type { Medication, MedicationRepository, Schedule, ScheduleRepository } from '../domain/Medication';
import { NativeAlarmService } from '../../alarms/infrastructure/NativeAlarmService';
import { SqliteDatabase } from '../../shared/infrastructure/SqliteDatabase';

export class MedicationService {
  constructor(
    private medicationRepo: MedicationRepository,
    private scheduleRepo: ScheduleRepository,
    private alarmService: NativeAlarmService
  ) {}

  async registerMedication(
    name: string, 
    time: string, 
    ringtoneUri: string, 
    color: string, 
    daysOfWeek: number[], 
    shouldTakePhoto: boolean = false, 
    existingId?: string
  ): Promise<void> {
    let photoUri: string | undefined;

    if (shouldTakePhoto) {
      try {
        const image = await Camera.getPhoto({
          quality: 90,
          allowEditing: false,
          resultType: CameraResultType.Uri
        });
        photoUri = image.webPath;
      } catch (e) {
        console.warn('No photo taken');
      }
    }

    const medicationId = existingId || crypto.randomUUID();
    
    let finalPhoto = photoUri;
    if (existingId && !photoUri) {
      const old = await this.medicationRepo.findById(existingId);
      finalPhoto = old?.photoUri;
    }

    const medication: Medication = {
      id: medicationId,
      name,
      photoUri: finalPhoto,
      color,
      daysOfWeek
    };
    await this.medicationRepo.save(medication);

    if (existingId) {
      const oldSchedules = await this.scheduleRepo.findByMedicationId(existingId);
      for (const s of oldSchedules) await this.scheduleRepo.delete(s.id);
    }

    const schedule: Schedule = {
      id: crypto.randomUUID(),
      medicationId: medication.id,
      time,
      systemRingtoneUri: ringtoneUri || ''
    };
    await this.scheduleRepo.save(schedule);

    await this.scheduleNextAlarm(medication, schedule);
  }

  private async scheduleNextAlarm(medication: Medication, schedule: Schedule) {
    const [hours, minutes] = schedule.time.split(':').map(Number);
    const trigger = new Date();
    trigger.setHours(hours, minutes, 0, 0);

    // Encontrar el siguiente día válido según daysOfWeek
    let attempts = 0;
    while (!medication.daysOfWeek.includes(trigger.getDay()) || trigger.getTime() <= Date.now()) {
      trigger.setDate(trigger.getDate() + 1);
      attempts++;
      if (attempts > 365) break; // Seguridad
    }

    await this.alarmService.scheduleAlarm(
      schedule.id,
      medication.name,
      schedule.systemRingtoneUri,
      trigger.getTime()
    );
  }

  async markAsTaken(scheduleId: string, date: Date): Promise<void> {
    const conn = SqliteDatabase.getInstance().getDb();
    const id = crypto.randomUUID();
    const timestamp = date.getTime();
    await conn.run(
      `INSERT INTO intake_logs (id, scheduleId, timestamp, status) VALUES (?, ?, ?, ?)`,
      [id, scheduleId, timestamp, 'TAKEN']
    );
  }

  async getIntakesForDate(date: Date): Promise<string[]> {
    const conn = SqliteDatabase.getInstance().getDb();
    const startOfDay = new Date(date);
    startOfDay.setHours(0, 0, 0, 0);
    const endOfDay = new Date(date);
    endOfDay.setHours(23, 59, 59, 999);
    
    const result = await conn.query(
      `SELECT scheduleId FROM intake_logs WHERE timestamp >= ? AND timestamp <= ? AND status = 'TAKEN'`,
      [startOfDay.getTime(), endOfDay.getTime()]
    );
    return (result.values || []).map((row: any) => row.scheduleId);
  }
}
