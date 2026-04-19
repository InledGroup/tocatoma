import type { Medication, MedicationRepository, Schedule, ScheduleRepository } from '../domain/Medication';
import { SqliteDatabase } from '../../shared/infrastructure/SqliteDatabase';

export class SqliteMedicationRepository implements MedicationRepository {
  private db = SqliteDatabase.getInstance();

  async save(medication: Medication): Promise<void> {
    const conn = this.db.getDb();
    await conn.run(
      `INSERT OR REPLACE INTO medications (id, name, photoUri, color, daysOfWeek) VALUES (?, ?, ?, ?, ?)`,
      [medication.id, medication.name, medication.photoUri || null, medication.color, JSON.stringify(medication.daysOfWeek)]
    );
  }

  async findById(id: string): Promise<Medication | null> {
    const conn = this.db.getDb();
    const result = await conn.query(`SELECT * FROM medications WHERE id = ?`, [id]);
    if (!result.values || result.values.length === 0) return null;
    const row = result.values[0];
    return { ...row, daysOfWeek: JSON.parse(row.daysOfWeek || '[]') } as Medication;
  }

  async findAll(): Promise<Medication[]> {
    const conn = this.db.getDb();
    const result = await conn.query(`SELECT * FROM medications`);
    return (result.values || []).map(row => ({
      ...row,
      daysOfWeek: JSON.parse(row.daysOfWeek || '[]')
    })) as Medication[];
  }

  async delete(id: string): Promise<void> {
    const conn = this.db.getDb();
    await conn.run(`DELETE FROM medications WHERE id = ?`, [id]);
  }
}

export class SqliteScheduleRepository implements ScheduleRepository {
  private db = SqliteDatabase.getInstance();

  async save(schedule: Schedule): Promise<void> {
    const conn = this.db.getDb();
    await conn.run(
      `INSERT OR REPLACE INTO schedules (id, medicationId, time, systemRingtoneUri) VALUES (?, ?, ?, ?)`,
      [schedule.id, schedule.medicationId, schedule.time, schedule.systemRingtoneUri]
    );
  }

  async findByMedicationId(medicationId: string): Promise<Schedule[]> {
    const conn = this.db.getDb();
    const result = await conn.query(`SELECT * FROM schedules WHERE medicationId = ?`, [medicationId]);
    return (result.values || []) as Schedule[];
  }

  async findAll(): Promise<Schedule[]> {
    const conn = this.db.getDb();
    const result = await conn.query(`SELECT * FROM schedules`);
    return (result.values || []) as Schedule[];
  }

  async delete(id: string): Promise<void> {
    const conn = this.db.getDb();
    await conn.run(`DELETE FROM schedules WHERE id = ?`, [id]);
  }
}
