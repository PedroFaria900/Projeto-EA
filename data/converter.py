import pandas as pd
import pickle
import uuid
import random
from datetime import datetime

print("A carregar ficheiros...")
stops_df = pd.read_csv('data/BusStopList.csv')
with open('data/BusRoutes.pickle', 'rb') as f:
    routes = pickle.load(f)
trans_df = pd.read_csv('data/BUS_DATA_OCT_2017.csv', nrows=50000)
print("Ficheiros carregados.")

lines = []

# ── PARAGENS ──────────────────────────────────────────────────────────────
lines.append("-- PARAGENS")
lines.append("TRUNCATE paragem CASCADE;")
stop_id_map = {}  # BUS_STOP -> uuid

for _, row in stops_df.iterrows():
    sid = str(uuid.uuid4())
    stop_id_map[row['BUS_STOP']] = sid
    lat = round(41.45 + random.uniform(0, 0.15), 6)
    lon = round(-8.50 + random.uniform(0, 0.15), 6)
    nome = row['BUS_STOP'].replace("'", "''")
    lines.append(
        f"INSERT INTO paragem (id, nome, codigo, latitude, longitude, municipio) "
        f"VALUES ('{sid}', '{nome}', '{nome}', {lat}, {lon}, 'CityX');"
    )

print(f"Paragens: {len(stop_id_map)}")

# ── LINHAS E LINHA_PARAGEM ────────────────────────────────────────────────
lines.append("\n-- LINHAS E LINHA_PARAGEM")
lines.append("TRUNCATE linha CASCADE;")
route_id_map = {}  # SER_xxx -> uuid

for route_code, df in routes.items():
    rid = str(uuid.uuid4())
    route_id_map[route_code] = rid
    designacao = route_code.replace("'", "''")
    lines.append(
        f"INSERT INTO linha (id, designacao, tipo_transporte) "
        f"VALUES ('{rid}', '{designacao}', 'AUTOCARRO');"
    )
    seq = 1
    seen = set()
    for _, r in df.iterrows():
        stop = r['Stop_stn']
        if stop not in stop_id_map or stop in seen:
            continue
        seen.add(stop)
        pid = stop_id_map[stop]
        tempo = int(r['sub'].total_seconds()) if hasattr(r['sub'], 'total_seconds') else 0
        lines.append(
            f"INSERT INTO linha_paragem (linha_id, paragem_id, sentido, sequencia, tempo_estimado_seg) "
            f"VALUES ('{rid}', '{pid}', 'IDA', {seq}, {tempo});"
        )
        seq += 1

print(f"Linhas: {len(route_id_map)}")

# ── UTENTES ───────────────────────────────────────────────────────────────
lines.append("\n-- UTENTES")
lines.append("TRUNCATE utente CASCADE;")
card_ids = trans_df['Card_Number'].unique()
utente_map = {}  # card_number -> uuid

for card in card_ids:
    uid = str(uuid.uuid4())
    utente_map[card] = uid
    email = f"utente_{card}@urbanbus.com"
    lines.append(
        f"INSERT INTO utente (id, nome, email, password_hash, saldo) "
        f"VALUES ('{uid}', 'Utente {card}', '{email}', "
        f"'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LzTfztsgEMO', 0.00);"
    )

print(f"Utentes: {len(utente_map)}")

# ── TITULOS, VALIDACOES E VIAGENS ─────────────────────────────────────────
lines.append("\n-- TITULOS TRANSPORTE")
lines.append("TRUNCATE titulo_transporte CASCADE;")
lines.append("TRUNCATE titulo_passe CASCADE;")
lines.append("TRUNCATE validacao CASCADE;")
lines.append("TRUNCATE viagem CASCADE;")

# Leitor fictício para dados históricos
leitor_id = str(uuid.uuid4())
leitor_paragem_id = list(stop_id_map.values())[0]
lines.append(f"\n-- LEITOR HISTÓRICO")
lines.append(f"INSERT INTO leitor (id, codigo, sentido, tipo) "
             f"VALUES ('{leitor_id}', 'HISTORICO-01', 'ENTRADA', 'LEITOR_FIXO');")
lines.append(f"INSERT INTO leitor_fixo (id, paragem_id, latitude, longitude) "
             f"VALUES ('{leitor_id}', '{leitor_paragem_id}', 41.55, -8.42);")

lines.append("\n-- TITULOS, VALIDACOES E VIAGENS")

skipped = 0
processed = 0

for _, row in trans_df.iterrows():
    card = row['Card_Number']
    boarding = row['Boarding_stop_stn']
    alighting = row['Alighting_stop_stn']

    if boarding not in stop_id_map or alighting not in stop_id_map:
        skipped += 1
        continue
    if card not in utente_map:
        skipped += 1
        continue

    uid = utente_map[card]
    tid = str(uuid.uuid4())
    vid_entrada = str(uuid.uuid4())
    vid_saida = str(uuid.uuid4())
    viagem_id = str(uuid.uuid4())

    boarding_pid = stop_id_map[boarding]
    alighting_pid = stop_id_map[alighting]

    try:
        inicio = datetime.strptime(
            f"{row['Ride_start_date']} {row['Ride_start_time']}", "%Y-%m-%d %H:%M:%S")
        fim = datetime.strptime(
            f"{row['Ride_end_date']} {row['Ride_end_time']}", "%Y-%m-%d %H:%M:%S")
    except Exception:
        skipped += 1
        continue

    # Titulo passe
    lines.append(
        f"INSERT INTO titulo_transporte (id, utente_id, estado, token_ativo, token_expira_em) "
        f"VALUES ('{tid}', '{uid}', 'ATIVO', NULL, NULL);"
    )
    lines.append(
        f"INSERT INTO titulo_passe (id, validade, area_geografica) "
        f"VALUES ('{tid}', '2018-12-31', 'ZonaTotal');"
    )

    # Validacao entrada
    lines.append(
        f"INSERT INTO validacao (id, titulo_id, leitor_id, paragem_id, momento, resultado, tipo_evento) "
        f"VALUES ('{vid_entrada}', '{tid}', '{leitor_id}', '{boarding_pid}', "
        f"'{inicio.strftime('%Y-%m-%d %H:%M:%S')}', 'VALIDO', 'ENTRADA');"
    )

    # Validacao saida
    lines.append(
        f"INSERT INTO validacao (id, titulo_id, leitor_id, paragem_id, momento, resultado, tipo_evento) "
        f"VALUES ('{vid_saida}', '{tid}', '{leitor_id}', '{alighting_pid}', "
        f"'{fim.strftime('%Y-%m-%d %H:%M:%S')}', 'VALIDO', 'SAIDA');"
    )

    # Viagem
    lines.append(
        f"INSERT INTO viagem (id, val_entrada_id, val_saida_id, inicio, fim) "
        f"VALUES ('{viagem_id}', '{vid_entrada}', '{vid_saida}', "
        f"'{inicio.strftime('%Y-%m-%d %H:%M:%S')}', '{fim.strftime('%Y-%m-%d %H:%M:%S')}');"
    )

    processed += 1

print(f"Viagens processadas: {processed}, ignoradas: {skipped}")

# ── ESCREVER FICHEIRO ─────────────────────────────────────────────────────
with open('data/seed.sql', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print(f"\nFicheiro data/seed.sql gerado com {len(lines)} linhas.")
print("Pronto!")