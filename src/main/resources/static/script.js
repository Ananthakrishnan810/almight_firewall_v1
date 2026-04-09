document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const btnPrimary = document.querySelector('.btn-primary');

  if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      
      // Simulate loading state
      const originalText = btnPrimary.textContent;
      btnPrimary.textContent = 'Authenticating...';
      btnPrimary.style.opacity = '0.8';
      btnPrimary.disabled = true;
      
      setTimeout(() => {
        // Reset state for demo purposes
        btnPrimary.textContent = originalText;
        btnPrimary.style.opacity = '1';
        btnPrimary.disabled = false;
        
        // Simulating error or success
        if (username && password) {
          console.log(`Login attempted for user: ${username}`);
          // Add your real authentication logic here
        }
      }, 1500);
    });
  }
  
  // Add simple interactive ripple effect to SSO button
  const ssoBtn = document.querySelector('.btn-sso');
  if (ssoBtn) {
    ssoBtn.addEventListener('click', function(e) {
      this.style.transform = 'scale(0.98)';
      setTimeout(() => {
        this.style.transform = 'scale(1)';
      }, 150);
      console.log('SSO login clicked');
    });
  }
});
