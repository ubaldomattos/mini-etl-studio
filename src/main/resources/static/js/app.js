(function () {
    'use strict';

    // ---- Tema claro/escuro (preferência salva no navegador) ----
    var botaoTema = document.getElementById('alternarTema');
    if (botaoTema) {
        botaoTema.addEventListener('click', function () {
            var atual = document.documentElement.getAttribute('data-bs-theme') === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', atual);
            try { localStorage.setItem('tema', atual); } catch (e) { /* armazenamento indisponível */ }
            document.dispatchEvent(new CustomEvent('tema-alterado', {detail: atual}));
        });
    }

    // ---- Upload: seleção por clique ou arrastar e soltar ----
    var dropzone = document.getElementById('dropzone');
    var inputArquivo = document.getElementById('arquivo');
    if (dropzone && inputArquivo) {
        dropzone.addEventListener('click', function () { inputArquivo.click(); });
        dropzone.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); inputArquivo.click(); }
        });
        ['dragenter', 'dragover'].forEach(function (ev) {
            dropzone.addEventListener(ev, function (e) { e.preventDefault(); dropzone.classList.add('arrastando'); });
        });
        ['dragleave', 'drop'].forEach(function (ev) {
            dropzone.addEventListener(ev, function (e) { e.preventDefault(); dropzone.classList.remove('arrastando'); });
        });
        dropzone.addEventListener('drop', function (e) {
            if (e.dataTransfer.files.length) {
                inputArquivo.files = e.dataTransfer.files;
                inputArquivo.dispatchEvent(new Event('change', {bubbles: true}));
            }
        });
        inputArquivo.addEventListener('change', function () {
            var nome = document.getElementById('nomeArquivoSelecionado');
            if (nome) { nome.textContent = inputArquivo.files.length ? inputArquivo.files[0].name : ''; }
        });
    }

    // Desabilita o botão de envio durante o processamento para evitar envios duplicados.
    document.querySelectorAll('form[data-processando]').forEach(function (form) {
        form.addEventListener('submit', function () {
            var botao = form.querySelector('button[type="submit"]');
            if (botao) {
                botao.disabled = true;
                botao.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>'
                    + form.getAttribute('data-processando');
            }
        });
    });
})();
