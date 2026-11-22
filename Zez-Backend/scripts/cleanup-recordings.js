const { Pool } = require('pg');
require('dotenv').config({ path: 'd:/Call Log Mannager/Zez-Backend/.env' });
const { S3Client, ListObjectsV2Command, DeleteObjectsCommand } = require('@aws-sdk/client-s3');

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

async function cleanupRecordings() {
  const bucket = process.env.B2_BUCKET_NAME;
  console.log(`Starting cleanup of silent call recordings from B2 bucket: ${bucket} and Neon PostgreSQL...`);

  try {
    // Step 1: Scan B2 for call_recordings/ prefix
    let continuationToken = null;
    let totalB2Deleted = 0;

    do {
      const listParams = {
        Bucket: bucket,
        Prefix: 'call_recordings/',
        ContinuationToken: continuationToken
      };
      const listRes = await s3Client.send(new ListObjectsV2Command(listParams));
      const objects = listRes.Contents || [];

      if (objects.length > 0) {
        console.log(`Found ${objects.length} B2 objects with prefix 'call_recordings/' to delete...`);
        const deleteParams = {
          Bucket: bucket,
          Delete: {
            Objects: objects.map(o => ({ Key: o.Key })),
            Quiet: false
          }
        };
        const delRes = await s3Client.send(new DeleteObjectsCommand(deleteParams));
        totalB2Deleted += (delRes.Deleted || []).length;
        console.log(`Deleted ${delRes.Deleted?.length || 0} objects from B2.`);
      }

      continuationToken = listRes.NextContinuationToken;
    } while (continuationToken);

    console.log(`Total B2 objects deleted: ${totalB2Deleted}`);

    // Step 2: Delete from Neon DB media_files
    const dbDeleteRes = await pool.query(
      "DELETE FROM public.media_files WHERE file_category = 'call_recordings' OR object_key LIKE 'call_recordings/%' RETURNING id, object_key, file_category"
    );
    console.log(`Total DB media_files rows deleted: ${dbDeleteRes.rowCount}`);

    // Step 3: Verification
    console.log('\n--- VERIFICATION ---');
    const remainingDb = await pool.query(
      "SELECT file_category, count(1) as count, sum(file_size) as total_size FROM public.media_files GROUP BY file_category"
    );
    console.log('Remaining categories in DB:', remainingDb.rows);

    const b2Check = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: 'call_recordings/'
    }));
    console.log(`Remaining B2 objects with prefix 'call_recordings/': ${(b2Check.Contents || []).length}`);

    // Double check root folders
    const rootCheck = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Delimiter: '/'
    }));
    console.log('B2 remaining root folders:', (rootCheck.CommonPrefixes || []).map(p => p.Prefix));

  } catch (err) {
    console.error('Error during cleanup:', err);
  } finally {
    await pool.end();
  }
}

cleanupRecordings();
