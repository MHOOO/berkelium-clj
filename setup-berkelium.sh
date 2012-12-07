#!/bin/zsh

JAVALINK=$(which -s java)
JAVABIN=$JAVALINK[(w)3]

JAVABASE=$(dirname $JAVABIN)/../..

cp ../../sources/berkelium.git/berkelium ../../sources/berkelium.git/chrome.pak ../../sources/berkelium.git/resources.pak ../../sources/berkelium.git/build/chromium/src/out/Release/libffmpegsumo.so ./browser
swig -v -Wall -Werror -c++ -package berkelium -outdir src/java/berkelium/ -java swig/berkelium.i

g++ -Wall -ggdb -fpic -c swig/berkelium_wrap.cxx -I$JAVABASE/include -I$JAVABASE/include/linux -I ../../sources/berkelium.git/include/ -lstdc++ -o swig/berkelium_wrap.o

if [[ $1 = "debug" ]]; then
    echo "Linking against DEBUG version of berkelium"
    cp  ../../sources/berkelium.git/liblibberkelium_d.so ./browser
    g++ -ggdb -fpic -shared swig/berkelium_wrap.o -L./browser -llibberkelium_d -o browser/libBerkeliumCppJavaWrap.so
else
    echo "Linking against RELEASE version of berkelium"
    cp ../../sources/berkelium.git/liblibberkelium.so ./browser
    g++ -ggdb -fpic -shared swig/berkelium_wrap.o -L./browser -llibberkelium -o browser/libBerkeliumCppJavaWrap.so
fi
