let currentPage = 1;
const ordersPerPage = 8;

function loadMoreOrders() {
    currentPage++;
    // 여기에 AJAX 또는 Fetch를 사용하여 추가 주문 내역을 불러오는 로직을 추가합니다.
    // 예시:
    fetch(`/loadOrders?page=${currentPage}`)
        .then(response => response.json())
        .then(data => {
            const ordersContainer = document.querySelector('.orders-container');
            data.orders.forEach(order => {
                const orderElement = document.createElement('div');
                orderElement.classList.add('container');
                orderElement.innerHTML = `
                    <img class="product-img" src="${order.imgSrc}" alt="..."/>
                    <div class="order-item">
                        <div class="order-details">
                            <p class="order-date">주문 날짜: ${order.date}</p>
                            <p class="order-product">상품명: ${order.productName}</p>
                            <p class="order-quantity">수량: ${order.quantity}개</p>
                            <p class="order-price">가격: ${order.price}원</p>
                            <p class="order-location">배송지: ${order.location}</p>
                            <p class="order-status">배송완료 여부: ${order.status}</p>
                        </div>
                        <div class="order-cancel">
                            <button onclick="cancelOrder()">주문 취소</button>
                        </div>
                    </div>
                `;
                ordersContainer.appendChild(orderElement);
            });
        });
}

function cancelOrder() {
    // 주문 취소 로직을 여기에 추가
}
