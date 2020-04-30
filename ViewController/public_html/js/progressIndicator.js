function deactivateProgressIndicators()
{
if (self._pollManager)
{
if (document.getElementById('fileUploadStatus').value=='noUpload')
{
_pollManager.deactivateAll();
}
}
}

function reactivateProgressIndicators()
{
if (self._pollManager)
{
document.getElementById('panelBox').style.display='';
document.getElementById('fileUploadStatus').value='uploading';
_pollManager.reactivateAll();
}
}