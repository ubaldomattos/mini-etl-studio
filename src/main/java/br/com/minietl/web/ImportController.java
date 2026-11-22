package br.com.minietl.web;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.exception.ArquivoInvalidoException;
import br.com.minietl.service.ProcessamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/** Upload, prévia e execução do processamento (Extract + Transform). */
@Controller
@RequestMapping("/importar")
public class ImportController {

    private final ProcessamentoService processamentoService;

    public ImportController(ProcessamentoService processamentoService) {
        this.processamentoService = processamentoService;
    }

    @GetMapping
    public String formulario(Model model) {
        model.addAttribute("ativo", "importar");
        return "importar";
    }

    /** Fragmento HTMX com a prévia (ou a mensagem de erro) do arquivo selecionado. */
    @PostMapping("/previa")
    public String previa(@RequestParam("arquivo") MultipartFile arquivo, Model model) {
        try {
            model.addAttribute("previa", processamentoService.gerarPrevia(
                    arquivo.getOriginalFilename(), lerConteudo(arquivo)));
            return "fragments/previa :: previa";
        } catch (ArquivoInvalidoException e) {
            model.addAttribute("mensagem", e.getMessage());
            return "fragments/previa :: erro";
        }
    }

    @PostMapping("/processar")
    public String processar(@RequestParam("arquivo") MultipartFile arquivo, RedirectAttributes redirect) {
        try {
            Processamento processamento = processamentoService.processar(
                    arquivo.getOriginalFilename(), lerConteudo(arquivo));
            redirect.addFlashAttribute("sucesso", "Arquivo processado: "
                    + processamento.getQtdExtraidos() + " registro(s) extraído(s). "
                    + "Revise o resultado e carregue os válidos no banco.");
            return "redirect:/processamentos/" + processamento.getId();
        } catch (ArquivoInvalidoException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/importar";
        }
    }

    private byte[] lerConteudo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("Nenhum arquivo selecionado ou o arquivo está vazio.");
        }
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new ArquivoInvalidoException("Não foi possível ler o arquivo enviado.", e);
        }
    }
}
