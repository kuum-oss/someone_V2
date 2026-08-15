<#import "layout.ftl" as layout>
<@layout.main_layout title="Замовлення підтверджено">
    <div class="container mt-5">
        <div class="card shadow-sm mx-auto" style="max-width: 600px;">
            <div class="card-header bg-success text-white text-center">
                <h4 class="mb-0">✅ Замовлення підтверджено!</h4>
            </div>
            <div class="card-body text-center">
                <h5 class="card-title">Замовлення #${order.id}</h5>
                <p class="card-text mb-1"><strong>Книга:</strong> ${order.bookTitle!""}</p>
                <#if order.seatNumber??>
                    <p class="card-text mb-1"><strong>Місце:</strong> ${order.seatNumber}</p>
                </#if>
                <#if order.startTime?? && order.endTime??>
                    <p class="card-text mb-3"><strong>Час:</strong> ${order.startTime.toLocalTime()} – ${order.endTime.toLocalTime()}</p>
                </#if>

                <div class="my-4">
                    <#if order.qrToken??>
                        <img src="/order/${order.id?c}/qr.png" alt="QR Code" class="img-fluid border p-2 rounded" style="max-width: 250px;">
                    <#else>
                        <div class="alert alert-warning">QR-код недоступний.</div>
                    </#if>
                </div>

                <p class="text-muted">📧 QR-код надіслано на вашу пошту.</p>
                <p class="text-muted">🔢 Або назвіть адміну номер: <strong>#${order.id}</strong></p>

                <div class="mt-4">
                    <a href="/admin/user/${order.userId?c}" class="btn btn-primary me-2">Мій кабінет</a>
                    <a href="/shop" class="btn btn-outline-secondary">Повернутися до магазину</a>
                </div>
            </div>
        </div>
    </div>
</@layout.main_layout>
