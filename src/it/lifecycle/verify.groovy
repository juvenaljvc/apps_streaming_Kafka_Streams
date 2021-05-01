File generated = new File( basedir, "target/classes/README.txt" );

assert generated.readLines().each { line -> assert line.startsWith('-') }
