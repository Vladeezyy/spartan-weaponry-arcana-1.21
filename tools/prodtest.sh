#!/bin/sh
# Release check: the jars as players have them (1.21.11: no Botania). NeoForge: the release jar (and Spartan
# Weaponry's from libs/) on the real NeoForge servers in prodtest/*/ (prodtest/serve.sh). Fabric: the release jar on a
# real Fabric server (:fabric:prodServer), then the client scene on a production client (intermediary names;
# :fabric:prodScene runs a jar that is the release jar plus port/). Outputs: prodtest/<version>/server*.out,
# build-prod*-fabric*.log.
. "$(dirname "$0")/common.sh"
MOD=spartanweaponryarcana
./gradlew :neoforge:jar :fabric:remapJar -q || exit 1
VERSION=$(grep '^mod_version=' gradle.properties | cut -d= -f2)+$(grep '^minecraft_version=' gradle.properties | cut -d= -f2)
JAR=neoforge/build/libs/$MOD-neoforge-$VERSION.jar
SW=libs/spartanweaponry-neoforge-$(grep '^spartanweaponry_version=' gradle.properties | cut -d= -f2).jar
for dir in prodtest/*/; do
	[ -f "$dir/run.sh" ] || continue
	find "$dir/mods" -iname "*weaponry*.jar" -delete 2>/dev/null
	find "$dir/mods" -iname "botania*.jar" -o -iname "Patchouli*.jar" -o -iname "curios*.jar" 2>/dev/null | xargs rm -f
	cp "$JAR" "$SW" "$dir/mods/"
	echo "=== NeoForge server $(grep -o 'neoforge/[0-9.]*' "$dir/run.sh" | head -1): $(ls "$dir/mods" | tr '\n' ' ')"
	(cd "$dir" && sh ../serve.sh && cp server.out server-alone.out)
done
for B in ""; do
	TAG=$([ -n "$B" ] && echo "-botania")
	echo "=== Fabric server$TAG"
	mkdir -p fabric/run-prod-server && echo "eula=true" > fabric/run-prod-server/eula.txt
	rm -rf fabric/run-prod-server/world fabric/run-prod-server/mods
	LOG=build-prodserver-fabric$TAG.log
	./gradlew :fabric:prodServer $B --console=plain > "$LOG" 2>&1 &
	i=0
	until grep -qE 'Done \(|Crash report|crash report|BUILD FAILED|Exception in server tick' "$LOG" 2>/dev/null || [ $i -gt 300 ]; do sleep 2; i=$((i+1)); done
	sleep 3
	pkill -f "fabric.addMods=.*$MOD-fabric" 2>/dev/null; sleep 3
	grep -E 'Done \(|Crash report|crash report|BUILD FAILED' "$LOG" | head -3
	grep -E '/(ERROR|FATAL)\]|Caused by|Exception' "$LOG" | grep -vE 'server.properties|No data fixer registered' | cut -c1-250 | sort | uniq -c | sort -rn | head -10
	mkdir -p fabric/run-prod
	[ -f fabric/run-prod/options.txt ] || cp fabric/run/options.txt fabric/run-prod/ 2>/dev/null
	rm -rf fabric/run-prod/saves/swa_scene fabric/run-prod/screenshots fabric/run-prod/mods
	LOG=build-prodscene-fabric$TAG.log
	run_watched "$LOG" 1500 :fabric:prodScene $B
	echo "=== Fabric production client$TAG: $(grep -c '\] CHECK' "$LOG") checks, $(grep -oE 'DONE, failed checks: [0-9]+' "$LOG")"
	grep -E 'CHECK FAIL' "$LOG" | sed 's/.*CHECK/CHECK/' | head -20
	grep -E 'Mixin apply failed|InvalidInjection|Caused by' "$LOG" | cut -c1-250 | sort -u | head -10
done
