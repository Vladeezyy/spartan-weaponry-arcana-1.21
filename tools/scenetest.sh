#!/bin/sh
# Runs the client test scene (port/DevScene, max 15 min) on LOADER=neoforge|fabric: screenshots in
# <loader>/run/screenshots/scene_*.png, then prints the functional checks and the errors of the run. A crash report
# stops the client. Extra gradle arguments are passed on (e.g. -Pscene_lang=ru_ru).
. "$(dirname "$0")/common.sh"
LOG=build-scene-$LOADER.log
rm -rf "$RUN/saves/swa_scene" "$RUN"/screenshots/scene_*.png "$LOG"
run_watched "$LOG" 900 ":$LOADER:runScene" "$@"
grep -E '\[scene\] (CHECK|DONE)' "$LOG" | sed -E 's/^.*\[scene\] //'
ls "$RUN/screenshots/" 2>/dev/null | grep scene_
echo "--- errors"
errors "$LOG"
