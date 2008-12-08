@ECHO OFF

START dyna.cmd server -port 50000
PAUSE 2
START dyna.cmd client -name p1 -server localhost -port 50000
START dyna.cmd client -name p2 -server localhost -port 50000
