<#import "layout.ftl" as layout>

<@layout.main_layout title="Вибір місця">
    <div style="max-width: 800px; margin: 0 auto;">
        <h1>🪑 Вибір місця та часу</h1>
        <p>Книга: <strong>${book.title}</strong></p>

        <form action="/shop/buy/physical" method="POST" id="seatForm">
            <input type="hidden" name="bookId" value="${book.id?c}">
            <input type="hidden" name="hour" id="formHour" value="${currentHour + 1}">
            <input type="hidden" name="duration" id="formDuration" value="${defaultDuration?string}">
            
            <div style="background: #fff; padding: 1.5rem; border-radius: 12px; border: 1px solid var(--border-color); margin-bottom: 2rem;">
                <div style="display: flex; gap: 1rem; flex-wrap: wrap; margin-bottom: 1.5rem;">
                    <div>
                        <label style="display: block; font-size: 14px; margin-bottom: 4px;">Час (сьогодні):</label>
                        <select id="hourSelect" class="btn btn-secondary" onchange="updateOccupied()">
                            <#list 0..23 as h>
                                <option value="${h}" <#if h == currentHour + 1>selected</#if>>${h}:00</option>
                            </#list>
                        </select>
                    </div>
                    <div>
                        <label style="display: block; font-size: 14px; margin-bottom: 4px;">Період (год):</label>
                        <select id="durationSelect" class="btn btn-secondary" onchange="updateOccupied()">
                            <#list availablePeriods?split(",") as p>
                                <option value="${p?trim}" <#if p?trim == defaultDuration?string>selected</#if>>${p?trim} год</option>
                            </#list>
                        </select>
                    </div>
                </div>

                <div id="seatsGrid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(60px, 1fr)); gap: 10px;">
                    <#list 1..totalSeats as i>
                        <#assign seatName = "M" + i>
                        <div class="seat-box">
                            <input type="radio" name="seatNumber" value="${seatName}" id="seat${i}" style="display: none;">
                            <label for="seat${i}" class="seat-label" data-seat="${seatName}" style="display: flex; align-items: center; justify-content: center; width: 60px; height: 60px; border: 2px solid #ddd; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
                                ${seatName}
                            </label>
                        </div>
                    </#list>
                </div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                <a href="/shop" class="btn btn-secondary">Скасувати</a>
                <button type="submit" class="btn btn-primary" id="submitBtn" disabled>Підтвердити замовлення</button>
            </div>
        </form>
    </div>

    <style>
        .seat-label:hover { border-color: var(--primary-color); background: #f0f7ff; }
        input[type="radio"]:checked + .seat-label { border-color: var(--primary-color); background: var(--primary-color); color: #fff; }
        .seat-label.occupied { background: #eee; color: #aaa; cursor: not-allowed; border-color: #ddd; }
        .seat-label.occupied:hover { border-color: #ddd; background: #eee; }
    </style>

    <script>
        const occupiedData = ${occupiedJson};
        
        function updateOccupied() {
            const hour = document.getElementById('hourSelect').value;
            const duration = document.getElementById('durationSelect').value;
            
            // Sync hidden inputs
            document.getElementById('formHour').value = hour;
            document.getElementById('formDuration').value = duration;

            // In a real app, we'd fetch via AJAX. For simplicity, we'll redirect to refresh with new time
            const bookId = "${book.id?c}";
            window.location.href = `/book/` + bookId + `/order?hour=` + hour + `&duration=` + duration;
        }

        document.querySelectorAll('input[name="seatNumber"]').forEach(input => {
            input.addEventListener('change', () => {
                document.getElementById('submitBtn').disabled = false;
            });
        });

        // Mark occupied
        const occupied = ${occupiedJson};
        occupied.forEach(seat => {
            const label = document.querySelector(`.seat-label[data-seat="` + seat + `"]`);
            if (label) {
                label.classList.add('occupied');
                const radio = document.getElementById(label.getAttribute('for'));
                if (radio) radio.disabled = true;
            }
        });
    </script>
</@layout.main_layout>
