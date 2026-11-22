const fs = require('fs');
const content = fs.readFileSync('d:/Call Log Mannager/Zez-Backend/src/public/admin/index.html', 'utf8');

console.log('--- Sections in Admin Dashboard ---');
const sections = content.match(/<(section|div)[^>]+id=["'](view[a-zA-Z0-9_-]+)["'][^>]*>/g) || [];
sections.forEach(s => console.log(s));

console.log('\n--- Header Nav Elements ---');
const navMatch = content.match(/<nav[^>]*>([\s\S]*?)<\/nav>/);
if (navMatch) console.log(navMatch[0]);

console.log('\n--- View IDs and containers ---');
const lines = content.split('\n');
lines.forEach((line, i) => {
  if (line.includes('id="tab') || line.includes('id="view') || line.includes('class="view-') || line.includes('section id="')) {
    console.log(`Line ${i + 1}: ${line.trim()}`);
  }
});

