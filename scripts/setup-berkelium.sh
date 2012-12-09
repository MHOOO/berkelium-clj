#!/bin/zsh

JAVALINK=$(which -s java)
JAVABIN=$JAVALINK[(w)3]

JAVABASE=$(dirname $JAVABIN)/../..
BERKELIUM_HOME=$1
SO_OUT_DIR=./src/main/resource
BROWSER_DIR=./browser
MODE="release"

echo "Setting up the Berkelium Java Wrapper library."
I="  "
echo $I "using java: $JAVABIN in directory $JAVABASE"
echo $I "Berkelium Home: $1"

if [ ! -e $BERKELIUM_HOME ]; then
    echo $I "ERROR: Could not find berkelium home directory! Did you specify the correct path as :berkelium-home inside your project.clj?"
    exit 1
fi

if [! -e $(which swig)]; then
    echo $I "ERROR: Could not find swig. Please install swig before running this task."
fi

if [ ! -e $BROWSER_DIR ]; then
    mkdir $BROWSER_DIR
fi
if [ ! -e $SO_OUT_DIR ]; then
    mkdir $SO_OUT_DIR
fi

cp $BERKELIUM_HOME/berkelium $BERKELIUM_HOME/chrome.pak $BERKELIUM_HOME/resources.pak $BROWSER_DIR
swig -v -Wall -Werror -c++ -package berkelium -outdir src/main/java/berkelium/ -java swig/berkelium.i

rm swig/berkelium_wrap.o
echo "Running g++ as: g++ -Wall -ggdb -fpic -c swig/berkelium_wrap.cxx -I$JAVABASE/include -I$JAVABASE/include/linux -I $BERKELIUM_HOME/include/ -lstdc++ -o swig/berkelium_wrap.o"
g++ -Wall -ggdb -fpic -c swig/berkelium_wrap.cxx -I$JAVABASE/include -I$JAVABASE/include/linux -I $BERKELIUM_HOME/include/ -lstdc++ -o swig/berkelium_wrap.o

if [ ! -e swig/berkelium_wrap.o ]; then
    echo $I "ERROR: g++ did not produce any output?"
    exit 1
fi

if [[ $MODE = "debug" ]]; then
    echo $I "Linking against DEBUG version of berkelium"
    LIB_NAME=libberkelium_d
else
    echo $I "Linking against RELEASE version of berkelium"
    LIB_NAME=libberkelium
fi

# cp $BERKELIUM_HOME/build/chromium/src/out/Release/libffmpegsumo.so $SO_OUT_DIR
# cp $BERKELIUM_HOME/lib$LIB_NAME.so $SO_OUT_DIR
g++ -ggdb -fpic -shared swig/berkelium_wrap.o -L$SO_OUT_DIR -l$LIB_NAME -o $SO_OUT_DIR/de.karolski.berkelium-clj.so

exit 0
