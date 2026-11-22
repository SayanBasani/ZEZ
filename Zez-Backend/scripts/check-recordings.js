const { Pool } = require('pg');
require('dotenv').config({ path: 'd:/Call Log Mannager/Zez-Backend/.env' });
const { S3Client, ListObjectsV2Command } = require('@aws-sdk/client-s3');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

const s3Client = new S3Client({
  endpoint: process.env.B2_ENDPOINT,
  region: process.env.B2_REGION || 'us-east-005',
  credentials: {
    accessKeyId: process.env.B2_KEY_ID || process.env.B2_APPLICATION_KEY_ID,
    secretAccessKey: process.env.B2_APP_KEY || process.env.B2_APPLICATION_KEY,
  },
});

async function main() {
  try {
    console.log('--- DB Check ---');
    const tables = await pool.query("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
    console.log('Tables:', tables.rows.map(r => r.table_name));

    const mfCols = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='media_files'");
    console.log('media_files columns:', mfCols.rows);

    const sfCols = await pool.query("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='storage_files'");
    console.log('storage_files columns:', sfCols.rows);

    const cats = await pool.query("SELECT file_category, count(1) as count, sum(file_size) as total_size FROM public.media_files GROUP BY file_category");
    console.log('Categories in media_files:', cats.rows);

    const recordings = await pool.query("SELECT id, object_key, original_file_name, file_size, created_at FROM public.media_files WHERE file_category = 'call_recordings' OR object_key LIKE 'call_recordings/%'");
    console.log(`Found ${recordings.rows.length} call_recordings rows in media_files:`);
    let totalDbBytes = 0;
    for (const r of recordings.rows) {
      totalDbBytes += Number(r.file_size || 0);
      console.log(`  ID: ${r.id}, Key: ${r.object_key}, Size: ${r.file_size}, Original: ${r.original_file_name}`);
    }
    console.log(`Total DB call_recordings size: ${(totalDbBytes / (1024 * 1024)).toFixed(2)} MB`);

    console.log('\n--- B2 Storage Check ---');
    const bucket = process.env.B2_BUCKET_NAME;
    console.log('Bucket:', bucket);

    const listCmd = new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: 'call_recordings/'
    });
    const b2Res = await s3Client.send(listCmd);
    const b2Objects = b2Res.Contents || [];
    console.log(`B2 objects with prefix 'call_recordings/': ${b2Objects.length}`);
    let totalB2Bytes = 0;
    for (const obj of b2Objects) {
      totalB2Bytes += obj.Size || 0;
      console.log(`  B2 Key: ${obj.Key}, Size: ${obj.Size}, Modified: ${obj.LastModified}`);
    }
    console.log(`Total B2 call_recordings size: ${(totalB2Bytes / (1024 * 1024)).toFixed(2)} MB`);

    // Also list root prefixes
    const listRootCmd = new ListObjectsV2Command({
      Bucket: bucket,
      Delimiter: '/'
    });
    const rootRes = await s3Client.send(listRootCmd);
    console.log('B2 CommonPrefixes (folders):', (rootRes.CommonPrefixes || []).map(p => p.Prefix));

  } catch (err) {
    console.error('Error during check:', err);
  } finally {
    await pool.end();
  }
}

main();
