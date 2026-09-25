<#import "layout.ftlh" as layout>
<@layout.page title="Gmail cleaner - Progress" refreshSeconds=inProgress?then(60, 0)>
    <div class="card">
        <#if inProgress>
            <div class="spinner" role="status" aria-label="Loading"></div>
            <p>Cleaning in progress</p>
        <#else>
            <p>Done.</p>
            <p><a href="/cleaner" class="link">Back to cleaner</a></p>
        </#if>
    </div>
</@layout.page>
