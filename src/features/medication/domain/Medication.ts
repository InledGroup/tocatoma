export interface Medication {
  readonly id: string;
  readonly name: string;
  readonly photoUri?: string;
  readonly color: string;
  readonly daysOfWeek: number[]; // [0,1,2,3,4,5,6] donde 0 es Domingo
}

export interface Schedule {
  readonly id: string;
  readonly medicationId: string;
  readonly time: string; // ISO string o "HH:mm"
  readonly systemRingtoneUri: string;
}

export interface IntakeLog {
  readonly id: string;
  readonly scheduleId: string;
  readonly timestamp: number;
  readonly status: 'TAKEN' | 'SNOOZED' | 'MISSED';
}

export interface MedicationRepository {
  save(medication: Medication): Promise<void>;
  findById(id: string): Promise<Medication | null>;
  findAll(): Promise<Medication[]>;
  delete(id: string): Promise<void>;
}

export interface ScheduleRepository {
  save(schedule: Schedule): Promise<void>;
  findByMedicationId(medicationId: string): Promise<Schedule[]>;
  findAll(): Promise<Schedule[]>;
  delete(id: string): Promise<void>;
}
