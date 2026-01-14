package com.tnh.baseware.core.mappers.doc;

import com.tnh.baseware.core.dtos.doc.DocumentDTO;
import com.tnh.baseware.core.entities.doc.Document;
import com.tnh.baseware.core.forms.doc.DocumentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IDocumentMapper extends IGenericMapper<Document, DocumentEditorForm, DocumentDTO> {

    @Override
    DocumentDTO entityToDTO(Document entity);

    @Override
    Document formToEntity(DocumentEditorForm form);
}
