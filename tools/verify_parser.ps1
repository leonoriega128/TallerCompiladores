$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $projectRoot

try {
    # Otra JVM permite comprobar los codigos de salida reales de Main.
    $mavenArgs = @(
        '-q',
        'test-compile',
        'exec:exec',
        '-Dexec.executable=java',
        '-Dexec.classpathScope=test',
        '-Dexec.args=-Djava.awt.headless=true -cp %classpath org.example.VerificarAnalizador'
    )
    & mvn @mavenArgs
    if ($LASTEXITCODE -ne 0) {
        throw "La verificacion del analizador DFD fallo (codigo $LASTEXITCODE)."
    }
}
finally {
    Pop-Location
}
