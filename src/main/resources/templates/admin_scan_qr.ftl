<#import "layout.ftl" as layout>
<@layout.main_layout title="Сканування QR-коду">
    <div class="container mt-4">
        <h2>Сканування QR-коду / Пошук замовлення</h2>
        <div class="card mb-4">
            <div class="card-body">
                <form action="/admin/scan-qr" method="POST" class="d-flex align-items-center">
                    <input type="text" name="query" class="form-control me-2" placeholder="Введіть вміст QR-коду або номер замовлення" required autofocus>
                    <button type="submit" class="btn btn-primary">Перевірити</button>
                </form>
            </div>
        </div>

        <#if error??>
            <div class="alert alert-danger">${error}</div>
        </#if>

        <#if foundOrder??>
            <div class="card shadow-sm">
                <div class="card-header bg-info text-white">
                    <h4 class="mb-0">Замовлення #${foundOrder.id}</h4>
                </div>
                <div class="card-body">
                    <p><strong>Користувач:</strong> ${foundOrder.userEmail!""}</p>
                    <p><strong>Книга:</strong> ${foundOrder.bookTitle!""}</p>
                    <p><strong>Місце:</strong> ${foundOrder.seatNumber!"-"}</p>
                    <#if foundOrder.startTime??>
                        <p><strong>Час:</strong> ${foundOrder.startTime.toLocalTime()} – ${foundOrder.endTime.toLocalTime()}</p>
                    </#if>
                    <p><strong>Статус:</strong> <span class="badge bg-secondary">${foundOrder.status}</span></p>

                    <#if foundOrder.status == 'PENDING' || foundOrder.status == 'SHIPPED'>
                        <form action="/admin/orders/${foundOrder.id?c}/status" method="POST" class="mt-3">
                            <input type="hidden" name="status" value="DELIVERED">
                            <button type="submit" class="btn btn-success">Видати книгу (DELIVERED)</button>
                        </form>
                    <#else>
                        <div class="alert alert-warning mt-3">Це замовлення не може бути видане (Статус: ${foundOrder.status}).</div>
                    </#if>
                </div>
            </div>
        </#if>
    </div>
</@layout.main_layout>
