import { registerPlugin } from '@capacitor/core';

export interface AlarmPlugin {
  setAlarm(options: { id: string; medicationName: string; ringtoneUri: string; triggerTime: number }): Promise<void>;
  pickRingtone(): Promise<{ uri: string }>;
}

const AlarmPlugin = registerPlugin<AlarmPlugin>('AlarmPlugin');

export class NativeAlarmService {
  async scheduleAlarm(id: string, medicationName: string, ringtoneUri: string, triggerTime: number): Promise<void> {
    await AlarmPlugin.setAlarm({ id, medicationName, ringtoneUri, triggerTime });
  }

  async pickRingtone(): Promise<string> {
    const result = await AlarmPlugin.pickRingtone();
    return result.uri;
  }
}
