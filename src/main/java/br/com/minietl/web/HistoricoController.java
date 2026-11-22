package br.com.minietl.web;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.exception.CargaException;
import br.com.minietl.exception.OperacaoInvalidaException;
import br.com.minietl.export.TipoExportacao;
import br.com.minietl.load.CargaService;
import br.com.minietl.service.HistoricoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Histórico de processamentos, detalhe (com exportação) e disparo da carga. */
@Controller
public class HistoricoController {

    private final HistoricoService historicoService;
    private final CargaService cargaService;

    public HistoricoController(HistoricoService historicoService, CargaService cargaService) {
        this.historicoService = historicoService;
        this.cargaService = cargaService;
    }

    @GetMapping("/processamentos")
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", historicoService.listar(pagina));
        model.addAttribute("ativo", "historico");
        return "historico";
    }

    @GetMapping("/processamentos/{id}")
    public String detalhe(@PathVariable Long id,
                          @RequestParam(required = false) SituacaoRegistro situacao,
                          @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("processamento", historicoService.obter(id));
        model.addAttribute("registros", historicoService.listarRegistros(id, situacao, pagina));
        model.addAttribute("situacaoFiltro", situacao);
        model.addAttribute("situacoes", SituacaoRegistro.values());
        model.addAttribute("tiposExportacao", TipoExportacao.values());
        model.addAttribute("ativo", "historico");
        return "processamento";
    }

    @PostMapping("/processamentos/{id}/carga")
    public String carregar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Processamento processamento = cargaService.carregar(id);
            redirect.addFlashAttribute("sucesso", processamento.getQtdCarregados()
                    + " registro(s) carregado(s) no banco de dados.");
        } catch (OperacaoInvalidaException | CargaException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/processamentos/" + id;
    }
}
