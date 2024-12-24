package file.service;

import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import file.entity.AttachmentFile;
import file.repository.AttachmentFileRepository;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Service {
	
	private final AmazonS3 amazonS3;
	private final AttachmentFileRepository fileRepository;
	
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    private final String DIR_NAME = "s3_data";
    private static final String DELIMITER = "_";
	private static final String DIR_DELIMITER = "/";

    // 파일 업로드
	@Transactional
	public void uploadS3File(MultipartFile file) throws Exception {

		System.out.println("upload file 호출");

		if(file == null) {
			throw new Exception("파일 전달 오류 발생");
		}

		// DB 저장
		String path = "/home/ubuntu/app/s3_data/" + DIR_NAME;
		String attachmentOriginalFileName = file.getOriginalFilename();
		String uuid = UUID.randomUUID().toString();
		String attachmentFileName = uuid + DELIMITER + attachmentOriginalFileName;
		Long attachmentFileSize = file.getSize();
		String filePath = path + DIR_DELIMITER + attachmentFileName;
		System.out.println(filePath);

		AttachmentFile attachmentFile = AttachmentFile.builder()
				.attachmentFileName(attachmentFileName)
				.attachmentFileSize(attachmentFileSize)
				.attachmentOriginalFileName(attachmentOriginalFileName)
				.build();

		Long fileNo = fileRepository.save(attachmentFile).getAttachmentFileNo();

		if (fileNo != null) {
			// 원하는 경로에 파일 저장 -> S3 전송 및 저장 (putObject)
			File uploadFile = new File(filePath);
			file.transferTo(uploadFile);

			// S3 전송 및 저장
			// bucketName
			// key : bucket 내부에 객체가 저장되는 경로 + 파일명
			String s3Key = DIR_NAME + DIR_DELIMITER + uploadFile.getName();
			amazonS3.putObject(bucketName, s3Key, uploadFile);

			if (uploadFile.exists()) {
				Files.delete(uploadFile.toPath());
				System.out.println("로컬 파일 삭제 완료");
			}
		}
	}
	
	// 파일 다운로드
	@Transactional
	public ResponseEntity<Resource> downloadS3File(long fileNo) throws UnsupportedEncodingException {
		AttachmentFile attachmentFile = null;
		Resource resource = null;

		try {
			// DB에서 파일 검색
			attachmentFile = fileRepository.findById(fileNo)
					.orElseThrow(() -> new NoSuchElementException("파일이 존재하지 않음"));

			// S3의 파일 가져오기 (getObject) -> 전달
			S3Object s3Object = amazonS3.getObject(bucketName, DIR_NAME + DIR_DELIMITER + attachmentFile.getAttachmentFileName());

			S3ObjectInputStream s3is = s3Object.getObjectContent();
			resource = new InputStreamResource(s3is);
		} catch (Exception e) {
			return new ResponseEntity<>(resource, HttpStatus.NO_CONTENT);
		}

		String encodedFileName = URLEncoder
				.encode(attachmentFile.getAttachmentOriginalFileName(), StandardCharsets.UTF_8)
				.replace("+", "%20");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.add(Headers.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName);

		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}
	
}