"""
Seed LexSign users into Supabase.
Uses PBKDF2WithHmacSHA256 matching Android SecurityUtils.kt
"""
import hashlib, os, base64, requests, json

SUPABASE_URL = "https://fyyqhtykapzdvugkluty.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZ5eXFodHlrYXB6ZHZ1Z2tsdXR5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM3MDkyMTQsImV4cCI6MjA4OTI4NTIxNH0.1LXmgoS5jomZUgEfeuapfyWn_RmZWJaOehpWLrCIdYQ"

HEADERS = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Content-Type": "application/json",
    "Prefer": "resolution=merge-duplicates,return=representation",
}

ITERATIONS = 10_000
KEY_LENGTH = 32  # 256 bits

def hash_password(plain: str) -> str:
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac("sha256", plain.encode(), salt, ITERATIONS, dklen=KEY_LENGTH)
    return base64.b64encode(salt).decode() + ":" + base64.b64encode(dk).decode()

USERS = [
    {
        "nombre": "Administrador LexSign",
        "email": "admin@lexsign.com",
        "password": "Admin123!",
        "tipo": "administrador",
    },
    {
        "nombre": "Carlos García",
        "email": "cliente@test.com",
        "password": "Cliente123!",
        "tipo": "cliente",
    },
    {
        "nombre": "Ana Martínez",
        "email": "abogado1@test.com",
        "password": "Abogado123!",
        "tipo": "abogado",
        "especialidad": "Derecho Civil",
        "experiencia": 5,
        "descripcion": "Especialista en derecho civil y contratos.",
        "telefono": "555-0001",
        "tarjeta": "AB-12345",
    },
    {
        "nombre": "Roberto Sánchez",
        "email": "abogado2@test.com",
        "password": "Abogado456!",
        "tipo": "abogado",
        "especialidad": "Derecho Penal",
        "experiencia": 8,
        "descripcion": "Especialista en derecho penal y litigios.",
        "telefono": "555-0002",
        "tarjeta": "AB-67890",
    },
]

def get_user(email: str):
    r = requests.get(
        f"{SUPABASE_URL}/rest/v1/users",
        headers=HEADERS,
        params={"email": f"eq.{email}", "select": "id,email,tipo"},
    )
    data = r.json()
    return data[0] if data else None

def update_password(email: str, plain_pw: str):
    new_hash = hash_password(plain_pw)
    r = requests.patch(
        f"{SUPABASE_URL}/rest/v1/users",
        headers={**HEADERS, "Prefer": "return=representation"},
        params={"email": f"eq.{email}"},
        json={"password": new_hash},
    )
    if r.status_code in (200, 204):
        data = r.json()
        return data[0] if data else {"email": email}, None
    return None, r.text

def upsert_user(u: dict):
    plain_pw = u.pop("password")
    existing = get_user(u["email"])
    if existing:
        return update_password(u["email"], plain_pw)
    u["password"] = hash_password(plain_pw)
    r = requests.post(
        f"{SUPABASE_URL}/rest/v1/users",
        headers={**HEADERS, "Prefer": "return=representation"},
        json=u,
    )
    if r.status_code in (200, 201):
        return r.json()[0] if r.json() else None, None
    return None, r.text

def verify_login(email: str, plain: str, tipo: str):
    r = requests.get(
        f"{SUPABASE_URL}/rest/v1/users",
        headers=HEADERS,
        params={"email": f"eq.{email}", "select": "password,tipo"},
    )
    data = r.json()
    if not data:
        return False, "Usuario no encontrado"
    row = data[0]
    if row["tipo"] != tipo:
        return False, f"tipo esperado={tipo}, actual={row['tipo']}"
    stored = row["password"]
    parts = stored.split(":")
    if len(parts) != 2:
        return False, "formato de hash inválido"
    salt = base64.b64decode(parts[0])
    stored_hash = base64.b64decode(parts[1])
    candidate = hashlib.pbkdf2_hmac("sha256", plain.encode(), salt, ITERATIONS, dklen=KEY_LENGTH)
    return candidate == stored_hash, ""

print("=" * 60)
print("CREANDO / ACTUALIZANDO USUARIOS EN SUPABASE")
print("=" * 60)

credentials = [
    ("admin@lexsign.com", "Admin123!", "administrador"),
    ("cliente@test.com", "Cliente123!", "cliente"),
    ("abogado1@test.com", "Abogado123!", "abogado"),
    ("abogado2@test.com", "Abogado456!", "abogado"),
]

for u in USERS:
    result, err = upsert_user(dict(u))  # copy so we don't mutate original
    if result:
        print(f"  Creado: {result.get('email')} [{result.get('tipo')}] id={result.get('id')}")
    else:
        print(f"  ERROR al crear {u['email']}: {err}")

print()
print("=" * 60)
print("VERIFICANDO INICIO DE SESION")
print("=" * 60)
for email, pwd, tipo in credentials:
    ok, msg = verify_login(email, pwd, tipo)
    status = "OK" if ok else f"FALLO ({msg})"
    print(f"  {email} -> {status}")
