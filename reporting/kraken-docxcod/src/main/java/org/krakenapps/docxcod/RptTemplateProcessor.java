package org.krakenapps.docxcod;

import java.util.HashMap;

public class RptTemplateProcessor {
	
	private HashMap<String, DataSource> dsMap = new HashMap<String, DataSource>();
	private FileDocumentSource docSource;

	public RptTemplateProcessor(Config defaultConfig) {
		// 작업 디렉토리를 로컬에 저장.

	}

	public void mergeDataSource(HashMap<String, DataSource> hashMap) {
		// 갖고 있는 데이터 소스 맵에 새 맵을 합성.
		// 이미 String key 가 dsMap 에 존재할 경우 덮어쓴다.

	}

	public void addDataSource(String string, TableDataSource tableDataSource) {
		// dsMap 에 요소를 추가.
		
	}

	public void setDocumentSource(FileDocumentSource fileDocumentSource) {
		// TODO Auto-generated method stub
		
	}

	public RptOutput generateOutput() {
		// 일단 전제 조건들을 체크 (위의 함수들이 제대로 호출되었는가)
		
		// 체크하고 나서 documentSource 로부터 docx 를 읽고 config.workingDir 아래에 파일 압축을 풀고
		 
		// 압축을 푼 파일을 적절히 바꾸고
		
		// 적절히 바뀐 파일들을 다시 압축을 하고 압축 푼 파일을 기반으로 RptOutput 객체를 만든다. 
		
		// cleanUp 함수를 부른다.
		
		// 만들어둔 RptOutput 객체를 리턴.
		
		return new RptOutput();
	}
	
	private void cleanUp()
	{
		// generateOutput 단계에서 만들어진 중간 결과물들을 모두 수거함. 
	}

}
