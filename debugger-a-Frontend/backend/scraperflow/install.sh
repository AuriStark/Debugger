#!/bin/bash

./gradlew distZip
unzip -o build/distributions/*.zip -d build/distributions/

rm -rf ~/opt/lib/scraperflow/
mkdir -p ~/opt/lib/scraperflow/
cp -r build/distributions/scraperflow*/* ~/opt/lib/scraperflow/
