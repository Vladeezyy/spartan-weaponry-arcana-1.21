#!/bin/sh
# Multiplayer check (max 15 min) on LOADER=neoforge|fabric: starts the dev server (<loader>/run-net/, the dev player
# "Dev" is an operator), waits for it, runs the client scene port/NetScene that joins it, then stops the server and
# prints the checks and errors of both. Extra gradle arguments are passed on to both (which the
# Terra Burst needs).
. "$(dirname "$0")/common.sh"
NET="$LOADER/run-net"
SLOG=build-netserver-$LOADER.log
CLOG=build-netclient-$LOADER.log
kill_dev
rm -rf "$NET/world" "$SLOG" "$CLOG"
mkdir -p "$NET" && echo "eula=true" > "$NET/eula.txt"
printf 'online-mode=false\nspawn-protection=0\n' > "$NET/server.properties"
# The offline UUID of "Dev"
uuid=$(python3 -c 'import hashlib,uuid;h=bytearray(hashlib.md5(b"OfflinePlayer:Dev").digest());h[6]=h[6]&0x0f|0x30;h[8]=h[8]&0x3f|0x80;print(uuid.UUID(bytes=bytes(h)))')
echo "[{\"uuid\":\"$uuid\",\"name\":\"Dev\",\"level\":4,\"bypassesPlayerLimit\":false}]" > "$NET/ops.json"
./gradlew ":$LOADER:runNetServer" --console=plain "$@" > "$SLOG" 2>&1 &
i=0
until grep -qE 'Done \(|Minecraft Crash Report|BUILD FAILED' "$SLOG" 2>/dev/null || [ $i -gt 300 ]; do sleep 2; i=$((i+1)); done
perl -e 'alarm 600; exec @ARGV' ./gradlew ":$LOADER:runNetClient" --console=plain "$@" > "$CLOG" 2>&1
kill_dev; sleep 3
wait 2>/dev/null
grep -E '\[netscene\] (CHECK|DONE)' "$CLOG" | sed -E 's/^.*\[netscene\] //'
echo "--- server errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception|lost connection|kicked' "$SLOG" | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -20
echo "--- client errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception|Disconnected' "$CLOG" | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -20
