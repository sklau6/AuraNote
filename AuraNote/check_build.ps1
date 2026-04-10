$env:JAVA_HOME = 'C:\Program Files\Java\jdk-18.0.1.1'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$gradleBat = 'C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat'
$result = & $gradleBat :app:compileDebugKotlin 2>&1
$result | Set-Content -Path 'compile_result.txt' -Encoding UTF8
