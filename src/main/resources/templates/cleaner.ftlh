<#import "layout.ftlh" as layout>
<@layout.page title="Gmail cleaner - Cleaner">
    <div class="card">
        <h2>Cleaner</h2>
        <p>Mark all unread messages in your mailbox as read.</p>
        <form method="post" action="/cleaner">
            <button type="submit" class="button">Start cleaning</button>
        </form>
        <p><a href="/" class="link">Back to main page</a></p>
    </div>
</@layout.page>
