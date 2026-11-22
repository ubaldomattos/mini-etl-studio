package br.com.minietl.web;

import br.com.minietl.exception.ArquivoInvalidoException;
import br.com.minietl.exception.RecursoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/** Tratamento centralizado de erros: páginas amigáveis e mensagens claras em requisições HTMX. */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final String limiteUpload;

    public GlobalExceptionHandler(@Value("${spring.servlet.multipart.max-file-size:5MB}") String limiteUpload) {
        this.limiteUpload = limiteUpload;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException e, HttpServletRequest request) {
        return resposta(request, HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage());
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    public ModelAndView arquivoInvalido(ArquivoInvalidoException e, HttpServletRequest request) {
        return resposta(request, HttpStatus.BAD_REQUEST, "Arquivo inválido", e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView arquivoGrande(HttpServletRequest request) {
        return resposta(request, HttpStatus.PAYLOAD_TOO_LARGE, "Arquivo muito grande",
                "O arquivo excede o tamanho máximo permitido (" + limiteUpload + ").");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView parametroInvalido(HttpServletRequest request) {
        return resposta(request, HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Um dos parâmetros informados é inválido.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView erroInesperado(Exception e, HttpServletRequest request) {
        if (e instanceof ErrorResponse erro) {
            // Erros do próprio Spring MVC (404 de recurso, método não suportado etc.) mantêm seu status.
            HttpStatus status = HttpStatus.resolve(erro.getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                return resposta(request, status, status == HttpStatus.NOT_FOUND ? "Não encontrado" : "Requisição inválida",
                        status == HttpStatus.NOT_FOUND ? "O endereço solicitado não existe."
                                : "A requisição não pôde ser atendida.");
            }
        }
        log.error("Erro inesperado em {}", request.getRequestURI(), e);
        return resposta(request, HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado",
                "Ocorreu um erro inesperado. Tente novamente; se persistir, consulte os logs da aplicação.");
    }

    private ModelAndView resposta(HttpServletRequest request, HttpStatus status, String titulo, String mensagem) {
        boolean htmx = request.getHeader("HX-Request") != null;
        // Em requisições HTMX responde 200 com o fragmento de erro, para que a mensagem seja exibida.
        ModelAndView mav = new ModelAndView(htmx ? "fragments/previa :: erro" : "erro");
        mav.setStatus(htmx ? HttpStatus.OK : status);
        mav.addObject("mensagem", mensagem);
        mav.addObject("titulo", titulo);
        mav.addObject("codigo", status.value());
        mav.addObject("ativo", "");
        return mav;
    }
}
