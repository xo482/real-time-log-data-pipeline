document.getElementById('search-button').addEventListener('click', function() {
    const formatName = document.getElementById('format-name').value.toLowerCase();

    // 로컬 스토리지에서 데이터 로드
    const storedData = JSON.parse(localStorage.getItem('logData')) || [];

    const filteredData = storedData.filter(item => item.name.toLowerCase().includes(formatName));

    const resultBody = document.getElementById('result-body');
    const noResultsMessage = document.getElementById('no-results-message');

    resultBody.innerHTML = ''; // 기존 결과를 지움

    if (filteredData.length > 0) {
        noResultsMessage.classList.add('hidden');
        filteredData.forEach(item => {
            const row = document.createElement('tr');

            const cellSelect = document.createElement('td');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.dataset.name = item.name;
            cellSelect.appendChild(checkbox);
            row.appendChild(cellSelect);

            const cellFormatName = document.createElement('td');
            cellFormatName.innerHTML = `<a href="logsetting.html?logId=${encodeURIComponent(item.name)}">${item.name}</a>`;
            cellFormatName.classList.add('format-name');
            row.appendChild(cellFormatName);

            const cellDescription = document.createElement('td');
            cellDescription.innerText = item.description;
            cellDescription.classList.add('description');
            row.appendChild(cellDescription);

            resultBody.appendChild(row);
        });
    } else {
        noResultsMessage.classList.remove('hidden');
    }
});

document.getElementById('log-settings-button').addEventListener('click', function() {
    window.location.href = 'logsetting.html';
});

document.getElementById('select-all').addEventListener('change', function(event) {
    const checkboxes = document.querySelectorAll('#result-body input[type="checkbox"]');
    checkboxes.forEach(checkbox => checkbox.checked = event.target.checked);
});

document.getElementById('delete-button').addEventListener('click', function() {
    const checkboxes = document.querySelectorAll('#result-body input[type="checkbox"]:checked');
    const namesToDelete = Array.from(checkboxes).map(checkbox => checkbox.dataset.name);
    deleteFromLocalStorage(namesToDelete);
    loadTableData();
});

// 로컬 스토리지에서 데이터 로드하여 테이블에 추가
window.addEventListener('DOMContentLoaded', function() {
    loadTableData();
});

function loadTableData() {
    const storedData = JSON.parse(localStorage.getItem('logData')) || [];
    const tableBody = document.getElementById('result-body');
    tableBody.innerHTML = ''; // 기존 내용 지우기
    storedData.forEach(item => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td><input type="checkbox" data-name="${item.name}"></td>
        <td><a href="logsetting.html?logId=${encodeURIComponent(item.name)}">${item.name}</a></td>
        <td>${item.description}</td>
      `;
      tableBody.appendChild(row);
    });
}

function deleteFromLocalStorage(names) {
    let existingData = JSON.parse(localStorage.getItem('logData')) || [];
    existingData = existingData.filter(item => !names.includes(item.name));
    localStorage.setItem('logData', JSON.stringify(existingData));
}
