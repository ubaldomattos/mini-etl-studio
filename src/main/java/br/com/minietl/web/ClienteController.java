package br.com.minietl.web;

import br.com.minietl.service.ClienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/** Consulta dos clientes carregados. Requisições HTMX recebem apenas o fragmento da tabela. */
@Controller
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clientes")
    public String listar(@RequestParam(name = "q", defaultValue = "") String termo,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestHeader(name = "HX-Request", required = false) String htmx, Model model) {
        model.addAttribute("pagina", clienteService.buscar(termo, pagina));
        model.addAttribute("termo", termo);
        model.addAttribute("ativo", "clientes");
        return htmx != null ? "clientes :: resultado" : "clientes";
    }

    @GetMapping("/clientes/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.obter(id));
        model.addAttribute("ativo", "clientes");
        return "cliente";
    }
}
