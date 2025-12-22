package com.tnh.baseware.core.mappers.doc;

import com.tnh.baseware.core.dtos.doc.FileDocumentDTO;
import com.tnh.baseware.core.entities.doc.FileDocument;
import com.tnh.baseware.core.forms.doc.FileDocumentEditorForm;
import com.tnh.baseware.core.mappers.IGenericMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IFileDocumentMapper extends IGenericMapper<FileDocument, FileDocumentEditorForm, FileDocumentDTO> {
}
