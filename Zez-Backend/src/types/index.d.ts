import type { Request, Response, NextFunction } from 'express';

// ==============================================================================
// 1. DATABASE ENTITY TYPES
// ==============================================================================

export interface DeviceRecord {
  id: string;
  androidid: string;
  persistentid: string;
  device_name: string | null;
  device_model: string | null;
  manufacturer: string | null;
  brand: string | null;
  platform: string;
  os_version: string | null;
  app_version: string | null;
  primary_phone_number: string | null;
  sim1_carrier: string | null;
  sim2_carrier: string | null;
  registration_status: 'active' | 'suspended' | 'deleted';
  last_seen_at: Date;
  created_at: Date;
  updated_at: Date;
}

export interface CallLogRecord {
  id: string;
  device_id: string;
  external_id_hash: string;
  call_timestamp: Date;
  encrypted_payload: string;
  created_at: Date;
}

export interface DecryptedCall {
  phoneNumber: string;
  name: string | null;
  type: 'INCOMING' | 'OUTGOING' | 'MISSED' | 'REJECTED' | 'UNKNOWN';
  duration: number; // in seconds
  timestamp: number; // unix milliseconds
  matchedContactName?: string | null;
}

export interface DecodedCallRecord {
  id: string;
  deviceId: string;
  callTimestamp: Date;
  createdAt: Date;
  deviceName: string;
  deviceModel: string | null;
  androidId: string | null;
  call: DecryptedCall | null;
  decryptionFailed: boolean;
}

export interface ChatUserRecord {
  id: string;
  phone_number: string;
  display_name: string;
  avatar_url: string | null;
  about: string | null;
  last_seen_at: Date;
  created_at: Date;
  updated_at: Date;
}

export interface ChatConversationRecord {
  id: string;
  user_a_id: string;
  user_b_id: string;
  last_message_text: string | null;
  last_message_at: Date | null;
  is_secret_mode: boolean;
  secret_session_id: string | null;
  secret_mode_updated_at: Date | null;
  secret_mode_by: string | null;
  created_at: Date;
  updated_at: Date;
}

export interface ChatMessageRecord {
  id: string;
  conversation_id: string;
  sender_id: string;
  receiver_id: string;
  message_text: string | null;
  media_type: 'none' | 'image' | 'video' | 'audio' | 'document';
  media_url: string | null;
  media_name: string | null;
  media_size: number | null;
  mode: 'normal' | 'secret';
  secret_session_id: string | null;
  status: 'sent' | 'delivered' | 'read';
  read_at: Date | null;
  created_at: Date;
}

export interface AppReleaseRecord {
  id: number;
  version_name: string;
  version_code: number;
  release_notes: string | null;
  file_name: string;
  file_size: number;
  download_url: string;
  created_at: Date;
}

export interface AdminUserRecord {
  id: string;
  username: string;
  password_hash: string;
  created_at: Date;
  updated_at: Date;
}

// ==============================================================================
// 2. API PAYLOAD & RESPONSE TYPES
// ==============================================================================

export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  error?: string;
  code?: string;
  data?: T;
}

export interface CallSyncBatchPayload {
  deviceId: string;
  calls: Array<{
    externalId: string;
    encryptedPayload: string;
    callTimestamp: string;
  }>;
}

export interface SendMessagePayload {
  senderId: string;
  receiverId: string;
  messageText?: string;
  mediaType?: 'none' | 'image' | 'video' | 'audio' | 'document';
  mediaUrl?: string;
  mediaName?: string;
  mediaSize?: number;
  mode?: 'normal' | 'secret';
  secretSessionId?: string;
}

export interface ConversationModePayload {
  userId: string;
  secretMode: boolean;
}

export interface AdminCallsQuery {
  limit?: number;
  page?: number;
  deviceId?: string;
  type?: string;
  year?: number;
  month?: number;
  from?: string;
  to?: string;
  q?: string;
}

export interface AdminChatExportFormat {
  format?: 'txt' | 'csv' | 'json';
}
