#!/usr/bin/bash
rm -f *preset*.java
find ../../src/main/resources/ -name '*preset*.xml' | while read preset; do
	./../../src/main/preset-po/convpreset.pl "$preset" > "`echo "$preset" | sed -e 's_src/main/resources/\(.*\).xml_target/generated-sources/\1.java_'`"
done
xgettext $*
