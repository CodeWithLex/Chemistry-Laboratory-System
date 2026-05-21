const https = require('https');
const dns = require('dns');

// Helper to convert IPv6 string to a BigInt representation
function ipv6ToBigInt(ip) {
  // Normalize address (expand ::)
  let expanded = ip;
  if (ip.includes('::')) {
    const parts = ip.split('::');
    const left = parts[0] ? parts[0].split(':') : [];
    const right = parts[1] ? parts[1].split(':') : [];
    const missing = 8 - (left.length + right.length);
    const middle = Array(missing).fill('0');
    expanded = [...left, ...middle, ...right].join(':');
  }
  
  const segments = expanded.split(':').map(hex => parseInt(hex, 16));
  let result = 0n;
  for (const segment of segments) {
    result = (result << 16n) + BigInt(segment);
  }
  return result;
}

function parseCidr(cidr) {
  const [ip, prefixStr] = cidr.split('/');
  const prefix = parseInt(prefixStr, 10);
  const ipBig = ipv6ToBigInt(ip);
  
  const mask = ((1n << BigInt(prefix)) - 1n) << BigInt(128 - prefix);
  const start = ipBig & mask;
  const end = start + (1n << BigInt(128 - prefix)) - 1n;
  return { start, end };
}

function ipInCidr(ip, cidr) {
  const ipBig = ipv6ToBigInt(ip);
  const { start, end } = parseCidr(cidr);
  return ipBig >= start && ipBig <= end;
}

const targetIp = '2406:da12:5ca:b702:eb89:ff01:2f56:de77';

https.get('https://ip-ranges.amazonaws.com/ip-ranges.json', (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    const json = JSON.parse(data);
    const matches = [];
    
    for (const prefix of json.ipv6_prefixes) {
      try {
        if (ipInCidr(targetIp, prefix.ipv6_prefix)) {
          matches.push(prefix);
        }
      } catch (e) {}
    }
    
    console.log('Matches found:');
    console.log(JSON.stringify(matches, null, 2));
  });
});
