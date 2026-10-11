// Get the form and the controls that need client-side validation.
const form = document.getElementById('registrationForm');
const fields = {
  name: document.getElementById('fullName'), roll: document.getElementById('rollNumber'),
  email: document.getElementById('email'), course: document.getElementById('course'),
  password: document.getElementById('password'), file: document.getElementById('document'), agree: document.getElementById('agree')
};
const errors = {
  name: document.getElementById('nameError'), roll: document.getElementById('rollError'),
  email: document.getElementById('emailError'), course: document.getElementById('courseError'),
  password: document.getElementById('passwordError'), file: document.getElementById('fileError'), agree: document.getElementById('agreeError')
};
const message = document.getElementById('formMessage');

function showError(key, text) {
  errors[key].textContent = text;
  const control = fields[key];
  const wrapper = control.closest('.field');
  if (wrapper) wrapper.classList.toggle('invalid', Boolean(text));
  control.setAttribute('aria-invalid', text ? 'true' : 'false');
}

// Password must contain uppercase, lowercase, number, symbol and 8 characters.
function passwordIsStrong(value) {
  return value.length >= 8 && /[A-Z]/.test(value) && /[a-z]/.test(value) && /\d/.test(value) && /[^A-Za-z0-9]/.test(value);
}

fields.password.addEventListener('input', () => {
  const value = fields.password.value;
  const bar = document.getElementById('strengthBar');
  const text = document.getElementById('strengthText');
  let score = 0;
  if (value.length >= 8) score++;
  if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++;
  if (/\d/.test(value)) score++;
  if (/[^A-Za-z0-9]/.test(value)) score++;
  bar.style.width = `${score * 25}%`;
  bar.style.background = score < 2 ? '#d94e4e' : score < 4 ? '#e5a126' : '#218653';
  text.textContent = value ? `Password strength: ${['weak', 'weak', 'fair', 'good', 'strong'][score]}` : 'Password strength: not entered';
  if (value && passwordIsStrong(value)) showError('password', '');
});

fields.file.addEventListener('change', () => {
  const file = fields.file.files[0];
  document.getElementById('fileLabel').textContent = file ? file.name : 'Choose a file to upload';
  if (file && file.size <= 2 * 1024 * 1024 && /\.(pdf|jpe?g|png)$/i.test(file.name)) showError('file', '');
});

form.addEventListener('submit', (event) => {
  event.preventDefault(); // Stop submission until every field has passed validation.
  let valid = true;
  const set = (key, error) => { showError(key, error); if (error) valid = false; };
  set('name', fields.name.value.trim().length >= 2 ? '' : 'Enter your full name (at least 2 characters).');
  set('roll', /^[A-Za-z0-9/-]{3,20}$/.test(fields.roll.value.trim()) ? '' : 'Enter a valid roll number (3–20 letters/numbers).');
  set('email', /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(fields.email.value.trim()) ? '' : 'Enter a valid email address.');
  set('course', fields.course.value ? '' : 'Please select a course.');
  set('password', passwordIsStrong(fields.password.value) ? '' : 'Use 8+ characters with uppercase, lowercase, a number and a symbol.');
  const file = fields.file.files[0];
  const fileOk = file && file.size <= 2 * 1024 * 1024 && /\.(pdf|jpe?g|png)$/i.test(file.name);
  set('file', fileOk ? '' : 'Choose a PDF, JPG or PNG file up to 2 MB.');
  set('agree', fields.agree.checked ? '' : 'Please confirm that your information is accurate.');

  if (!valid) {
    message.textContent = 'Please correct the highlighted fields and try again.';
    message.className = 'form-message error-message';
    const firstInvalid = form.querySelector('[aria-invalid="true"]');
    if (firstInvalid) firstInvalid.focus();
    return;
  }
  // When opened directly as a file, show a demo message. When served from a web server,
  // submit to the configured backend (PHP by default) after client-side validation.
  if (window.location.protocol !== 'file:') {
    form.submit(); // Native submit avoids running this validation handler a second time.
  } else {
    message.textContent = 'Validation passed. Run this form through PHP or a Servlet server to save the application.';
    message.className = 'form-message success';
  }
});
