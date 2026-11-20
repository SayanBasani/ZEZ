const { pool, hashPassword } = require("../src/db");

(async () => {
  try {
    const cols = await pool.query(`
      SELECT column_name, data_type 
      FROM information_schema.columns 
      WHERE table_name = 'admin_users'
    `);
    console.log("admin_users columns:", cols.rows);
    
    const hash = hashPassword("123456");
    await pool.query(`
      INSERT INTO public.admin_users (username, password_hash)
      VALUES ($1, $2)
      ON CONFLICT (username) DO UPDATE
      SET password_hash = EXCLUDED.password_hash;
    `, ["sayan", hash]);
    
    console.log("User sayan set with password 123456 successfully!");
    
    const verify = await pool.query("SELECT * FROM public.admin_users WHERE username = 'sayan'");
    console.log("Verified user sayan:", verify.rows);
    
    await pool.end();
    process.exit(0);
  } catch (err) {
    console.error("Error setting admin user:", err);
    process.exit(1);
  }
})();
