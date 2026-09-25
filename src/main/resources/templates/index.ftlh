<#import "layout.ftlh" as layout>
<@layout.page>
    <div class="card">
        <#if email??>
            <p>Signed in as <strong>${email}</strong></p>
            <div class="primary-action">
                <a href="/cleaner" class="button button-large">
                    <svg class="button-icon" viewBox="0 0 24 24" aria-hidden="true">
                        <path fill="currentColor" d="M16 11h-1V3c0-1.1-.9-2-2-2h-2c-1.1 0-2 .9-2 2v8H8c-2.76 0-5 2.24-5 5v7h18v-7c0-2.76-2.24-5-5-5zm3 10h-2v-3c0-.55-.45-1-1-1s-1 .45-1 1v3h-2v-3c0-.55-.45-1-1-1s-1 .45-1 1v3H9v-3c0-.55-.45-1-1-1s-1 .45-1 1v3H5v-5c0-1.65 1.35-3 3-3h8c1.65 0 3 1.35 3 3v5z"/>
                    </svg>
                    <span>Cleaning</span>
                </a>
            </div>
            <form method="post" action="/logout" style="margin-top: 40px;">
                <button type="submit" class="google-button">
                    <@layout.googleIcon/>
                    <span>Sign out</span>
                </button>
            </form>
        <#else>
            <p>Authorize access to your Gmail account to continue.</p>
            <a href="/login" class="google-button">
                <@layout.googleIcon/>
                <span>Sign in with Google</span>
            </a>
        </#if>
    </div>
</@layout.page>
