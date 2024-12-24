package file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class AttachmentFile extends BaseEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "attachment_file_no")
	private Long attachmentFileNo; 				// 파일 번호
	
	@Column(name = "file_path")
	private String filePath;					// 파일 저장 경로(base경로 + 새로운 폴더 경로)
	
	@Column(name = "attachment_file_name")
	private String attachmentFileName;			// 파일명(UUID + 원본파일명)
	
	@Column(name = "attachment_original_file_name")
	private String attachmentOriginalFileName;	// 원본 파일명
	
	@Column(name = "attachment_file_size")
	private Long attachmentFileSize; 			// 파일 크기
	
	@Builder
	public AttachmentFile(long attachmentFileNo, String filePath, String attachmentFileName,
			String attachmentOriginalFileName, Long attachmentFileSize) {
		this.attachmentFileNo = attachmentFileNo;
		this.filePath = filePath;
		this.attachmentFileName = attachmentFileName;
		this.attachmentOriginalFileName = attachmentOriginalFileName;
		this.attachmentFileSize = attachmentFileSize;
	}
}