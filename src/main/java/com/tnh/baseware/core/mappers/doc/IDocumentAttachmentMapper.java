package com.tnh.baseware.core.mappers.doc;

import com.tnh.baseware.core.dtos.basic.BasicDocumentDTO;
import com.tnh.baseware.core.dtos.doc.DocumentAttachmentDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.entities.doc.DocumentAttachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IDocumentAttachmentMapper {

    @Mapping(source = "document", target = "document", qualifiedByName = "mapDocument")
    DocumentAttachmentDTO entityToDTO(DocumentAttachment entity);

    @Named("mapDocument")
    default BasicDocumentDTO mapDocument(Document document) {
        return document == null ? null
                : BasicDocumentDTO.builder()
                        .id(document.getId())
                        .documentNumber(document.getDocumentNumber())
                        .summary(document.getSummary())
                        .build();
    }
}
