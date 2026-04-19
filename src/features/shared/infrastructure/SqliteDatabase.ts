import { CapacitorSQLite, SQLiteConnection, SQLiteDBConnection } from '@capacitor-community/sqlite';

export class SqliteDatabase {
  private static instance: SqliteDatabase;
  private connection: SQLiteConnection;
  private db: SQLiteDBConnection | null = null;
  private isInitializing: Promise<void> | null = null;
  private readonly DB_NAME = 'tocatoma';

  private constructor() {
    this.connection = new SQLiteConnection(CapacitorSQLite);
  }

  public static getInstance(): SqliteDatabase {
    if (!SqliteDatabase.instance) {
      SqliteDatabase.instance = new SqliteDatabase();
    }
    return SqliteDatabase.instance;
  }

  public async init(): Promise<void> {
    if (this.isInitializing) return this.isInitializing;

    this.isInitializing = (async () => {
      if (this.db) return;

      try {
        const connections = await this.connection.checkConnectionsConsistency();
        const isConn = (await this.connection.isConnection(this.DB_NAME, false)).result;
        
        if (isConn && connections.result) {
          this.db = await this.connection.retrieveConnection(this.DB_NAME, false);
        } else {
          this.db = await this.connection.createConnection(this.DB_NAME, false, 'no-encryption', 1, false);
        }
        
        await this.db.open();

        // Esquema consolidado. No usamos ALTER TABLE para evitar errores de duplicidad.
        await this.db.execute(`
          CREATE TABLE IF NOT EXISTS medications (
            id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            photoUri TEXT,
            color TEXT,
            daysOfWeek TEXT
          );
          CREATE TABLE IF NOT EXISTS schedules (
            id TEXT PRIMARY KEY,
            medicationId TEXT NOT NULL,
            time TEXT NOT NULL,
            systemRingtoneUri TEXT NOT NULL,
            FOREIGN KEY (medicationId) REFERENCES medications(id) ON DELETE CASCADE
          );
          CREATE TABLE IF NOT EXISTS intake_logs (
            id TEXT PRIMARY KEY,
            scheduleId TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            status TEXT NOT NULL,
            FOREIGN KEY (scheduleId) REFERENCES schedules(id) ON DELETE CASCADE
          );
        `);
        console.log('Database Ready');
      } catch (e) {
        console.error('SQLite critical error', e);
        this.isInitializing = null;
        // No lanzamos error para no bloquear el hilo principal de la UI
      }
    })();

    return this.isInitializing;
  }

  public getDb(): SQLiteDBConnection {
    if (!this.db) throw new Error('Database not initialized');
    return this.db;
  }
}
