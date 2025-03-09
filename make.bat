xas99.py src/main-memory.a99 -i -q -R -L aticatac.lst -E symbols.txt -o bin/main
xas99.py src/rom-banks.a99 -B -q -R -o bin/banks.bin

java -jar tools/ea5tocart.jar bin\main "ATICATAC"

copy /b bin\main8.bin ^
    + bin\banks.bin ^
    aticatac8.bin

java -jar tools/CopyHeader.jar aticatac8.bin 60

jar -cvf aticatac.rpk aticatac8.bin layout.xml > make.log

