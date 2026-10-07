$ErrorActionPreference = 'Continue'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $projectRoot

try {
    $cases = @(
        @{ Name = 'programa_informe1'; Path = 'src/test/resources/programa_informe1.dfd'; ExpectedSyntaxError = $false }
        @{ Name = 'precedencia_y_constructos'; Path = 'src/test/resources/casos/sintaxis_precedencia_valida.dfd'; ExpectedSyntaxError = $false }
        @{ Name = 'comparacion_encadenada'; Path = 'src/test/resources/casos/sintaxis_comparacion_encadenada.dfd'; ExpectedSyntaxError = $true }
        @{ Name = 'punto_y_coma_faltante'; Path = 'src/test/resources/casos/sintaxis_punto_y_coma_faltante.dfd'; ExpectedSyntaxError = $true }
    )

    foreach ($case in $cases) {
        $mavenArgs = @(
            '-q',
            'exec:java',
            '-Dexec.classpathScope=test',
            '-Dexec.mainClass=org.antlr.v4.gui.TestRig',
            "-Dexec.args=org.example.DFD programa -tree $($case.Path)"
        )
        $output = @(& mvn @mavenArgs 2>&1 | ForEach-Object { "$($_)" })
        $exitCode = $LASTEXITCODE
        $diagnostics = @($output | Where-Object { $_ -match 'line \d+:\d+ ' })
        $combinedOutput = $output -join "`n"

        if ($exitCode -ne 0) {
            throw "TestRig no pudo ejecutar '$($case.Name)' (código $exitCode).`n$($output -join "`n")"
        }
        if ($combinedOutput -notmatch '(?m)^\(programa(?:\s|$)') {
            throw "TestRig no produjo árbol de análisis para '$($case.Name)'.`n$combinedOutput"
        }
        if ($combinedOutput -match 'token recognition error') {
            throw "La entrada '$($case.Name)' contiene errores léxicos y no permite aislar la prueba sintáctica.`n$combinedOutput"
        }

        $hasSyntaxError = $diagnostics.Count -gt 0
        if ($hasSyntaxError -ne $case.ExpectedSyntaxError) {
            throw "Resultado inesperado para '$($case.Name)'.`n$($output -join "`n")"
        }

        $result = if ($hasSyntaxError) { 'RECHAZADO' } else { 'ACEPTADO' }
        "{0}: {1}; errores sintacticos={2}" -f $case.Name, $result, $diagnostics.Count
        foreach ($diagnostic in $diagnostics) {
            "  $diagnostic"
        }
    }
}
finally {
    Pop-Location
}
