<#import "layout.ftlh" as layout>
<@layout.page title="Gmail cleaner - Error">
    <div class="card">
        <h2>Something went wrong</h2>
        <p class="error">${error!"Unknown error."}</p>
        <a href="/login" class="google-button">
            <@layout.googleIcon/>
            <span>Try again with Google</span>
        </a>
    </div>
</@layout.page>
