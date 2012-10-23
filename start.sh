coffee -wco shellgame-assets/js shellgame-assets &
PID1="$!"

coffee -wco shellgame/www/js shellgame/www/coffee &
PID2="$!"

sbt "run 9090 shellgame-assets/ shellgame/www/"

kill -9 $PID1
kill -9 $PID2