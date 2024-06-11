// 수정 버튼 클릭 시 이름과 설명을 로컬 스토리지에 저장하고 log.html로 이동
document.getElementById('update').addEventListener('click', function() {
  const originalName = new URLSearchParams(window.location.search).get('logId');
  const name = document.getElementById('name').value;
  const description = document.getElementById('description').value;
  const logPreview = document.getElementById('log-preview').value;
  const jsonLog = document.getElementById('json-log').value;
  if (name && description) {
    updateLocalStorage(originalName, name, description, logPreview, jsonLog);
    alert('이름과 설명이 수정되었습니다.');
    window.location.href = 'log.html'; // log.html로 이동
  } else {
    alert('이름과 설명을 입력해주세요.');
  }
});

// 로컬 스토리지에 데이터 수정
function updateLocalStorage(originalName, name, description, logContent, jsonContent) {
  const existingData = JSON.parse(localStorage.getItem('logData')) || [];
  const logIndex = existingData.findIndex(item => item.name === originalName);
  const logEntry = { name, description, logContent, jsonContent };
  if (logIndex > -1) {
    existingData[logIndex] = logEntry; // Update existing entry
  }
  localStorage.setItem('logData', JSON.stringify(existingData));
}

// 적용 버튼 클릭 시 이름과 설명을 로컬 스토리지에 저장하고 log.html로 이동
document.getElementById('apply').addEventListener('click', function() {
  const name = document.getElementById('name').value;
  const description = document.getElementById('description').value;
  const logPreview = document.getElementById('log-preview').value;
  const jsonLog = document.getElementById('json-log').value;
  if (name && description) {
    if (saveToLocalStorage(name, description, logPreview, jsonLog)) {
      alert('이름과 설명이 저장되었습니다.');
      window.location.href = 'log.html'; // log.html로 이동
    } else {
      alert('이름이 중복되었습니다. 다른 이름을 입력해주세요.');
    }
  } else {
    alert('이름과 설명을 입력해주세요.');
  }
});

// 로컬 스토리지에 데이터 저장 (중복 허용 안 함)
function saveToLocalStorage(name, description, logContent, jsonContent) {
  const existingData = JSON.parse(localStorage.getItem('logData')) || [];
  const logIndex = existingData.findIndex(item => item.name === name);
  if (logIndex === -1) {
    const logEntry = { name, description, logContent, jsonContent };
    existingData.push(logEntry);
    localStorage.setItem('logData', JSON.stringify(existingData));
    return true; // 저장 성공
  } else {
    return false; // 중복된 이름 존재
  }
}

// URL 파라미터에서 로그 정보를 가져와 표시하는 함수
function displayLogInfo() {
  const urlParams = new URLSearchParams(window.location.search);
  const logId = urlParams.get('logId');
  if (logId) {
    const logs = JSON.parse(localStorage.getItem('logData')) || [];
    const log = logs.find(item => item.name === logId);
    if (log) {
      document.getElementById('name').value = log.name;
      document.getElementById('description').value = log.description;
      document.getElementById('log-preview').value = log.logContent || '';
      document.getElementById('json-log').value = log.jsonContent || '';
    }
  }
}

// 파일 선택 시 로그 기록 미리보기에 파일 내용 표시
document.getElementById('file-input').addEventListener('change', function(event) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function(e) {
      const logPreview = document.getElementById('log-preview');
      logPreview.value = e.target.result;
    };
    reader.readAsText(file);
  }
});

// '찾아보기' 버튼 클릭 시 파일 입력 클릭 트리거
document.getElementById('browse-button').addEventListener('click', function() {
  document.getElementById('file-input').click();
});

// 포맷 적용 버튼 클릭 시 변환된 JSON 로그 표시
document.getElementById('apply-format').addEventListener('click', function() {
  const logPreview = document.getElementById('log-preview').value;
  const jsonLog = parseLogToJSON(logPreview);
  document.getElementById('json-log').textContent = JSON.stringify(jsonLog, null, 4);
});

// 로그 문자열을 JSON 로그로 변환하는 함수
function parseLogToJSON(logEntry) {
  const logJson = {};

  // Extract timestamp
  const timestampPattern = /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+ \+\d{4})/;
  const timestampMatch = logEntry.match(timestampPattern);
  if (timestampMatch) {
    logJson.timestamp = timestampMatch[1];
  }

  // Extract userAgent
  const userAgentPattern = /HTTP_USERAGENT":"([^"]+)"/;
  const userAgentMatch = logEntry.match(userAgentPattern);
  if (userAgentMatch) {
    logJson.userAgent = userAgentMatch[1];
  }

  // Extract acceptLanguage
  const acceptLanguagePattern = /HTTP_ACCEPT_LANGUAGE":"([^"]+)"/;
  const acceptLanguageMatch = logEntry.match(acceptLanguagePattern);
  if (acceptLanguageMatch) {
    logJson.acceptLanguage = acceptLanguageMatch[1];
  }

  // Extract message parameters
  const messagePattern = /message":"([^"]+)"/;
  const messageMatch = logEntry.match(messagePattern);
  if (messageMatch) {
    const message = decodeURIComponent(messageMatch[1]);
    const params = new URLSearchParams(message);

    logJson.userId = params.get('uid');
    logJson.screenResolution = params.get('res');
    logJson.url = params.get('url');
    logJson.referrerUrl = params.get('urlref');
    logJson.eventCategory = params.get('e_c');

    logJson.performanceMetrics = {
      networkTime: parseInt(params.get('pf_net'), 10),
      serverResponseTime: parseInt(params.get('pf_srt'), 10),
      transferTime: parseInt(params.get('pf_tfr'), 10),
      onLoadTime: parseInt(params.get('pf_onl'), 10),
      domContentLoadedTime: parseInt(params.get('pf_dm1'), 10)
    };
  }

  return logJson;
}

// 페이지가 로드될 때 로그 정보를 표시
window.addEventListener('DOMContentLoaded', displayLogInfo);
