package com.demo_rentas.Models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocService {
    private String tipoDocumento;
    private String tipoIdentificacionC;
    private String tipoDocumentoC;
    private String tipoIdentificacionB;
    private String tipoDocumentoB;
   private String tipoArchivo;
   private String codigoTransaccion;

}
