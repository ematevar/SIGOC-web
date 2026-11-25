package com.sigoc.sigoc_web.infrastructure.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_evento")
public class AuditoriaEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Column(name = "id_usuario", updatable = false)
    private Integer idUsuario;

    @Column(name = "usuario_login", length = 60, updatable = false)
    private String usuarioLogin;

    @Column(name = "rol", length = 40, updatable = false)
    private String rol;

    @Column(name = "accion", nullable = false, length = 60, updatable = false)
    private String accion;

    @Column(name = "entidad", length = 60, updatable = false)
    private String entidad;

    @Column(name = "id_entidad", length = 60, updatable = false)
    private String idEntidad;

    @Column(name = "resultado", nullable = false, length = 20, updatable = false)
    private String resultado;

    @Column(name = "detalle", length = 500, updatable = false)
    private String detalle;

    protected AuditoriaEventoEntity() {
    }

    public AuditoriaEventoEntity(LocalDateTime fechaHora, Integer idUsuario, String usuarioLogin, String rol,
                                 String accion, String entidad, String idEntidad, String resultado, String detalle) {
        this.fechaHora = fechaHora;
        this.idUsuario = idUsuario;
        this.usuarioLogin = usuarioLogin;
        this.rol = rol;
        this.accion = accion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.resultado = resultado;
        this.detalle = detalle;
    }

    public Long getIdEvento() { return idEvento; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public Integer getIdUsuario() { return idUsuario; }
    public String getUsuarioLogin() { return usuarioLogin; }
    public String getRol() { return rol; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public String getIdEntidad() { return idEntidad; }
    public String getResultado() { return resultado; }
    public String getDetalle() { return detalle; }
}
