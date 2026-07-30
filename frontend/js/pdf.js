/** Client-side PDF generation for booking confirmations (requires jsPDF on page). */
const BookingPdf = {
  download(booking) {
    if (!window.jspdf?.jsPDF) {
      Utils.toast('PDF library not loaded', 'error');
      return;
    }

    const doc = new window.jspdf.jsPDF();
    const name = CONFIG.businessName || 'Noko Transport';
    let y = 20;

    const line = (label, value) => {
      doc.setFont('helvetica', 'bold');
      doc.text(label, 20, y);
      doc.setFont('helvetica', 'normal');
      doc.text(String(value ?? 'N/A'), 70, y);
      y += 8;
    };

    doc.setFontSize(18);
    doc.setTextColor(11, 60, 93);
    doc.text(name, 20, y);
    y += 10;

    doc.setFontSize(14);
    doc.text('Booking Confirmation', 20, y);
    y += 12;

    doc.setFontSize(11);
    doc.setTextColor(0, 0, 0);

    line('Reference:', booking.bookingReference);
    line('Customer:', booking.customerName);
    line('Phone:', booking.customerPhone);
    line('Email:', booking.customerEmail);
    line('Truck:', `${booking.truckNumber} (${booking.truckRegistration})`);
    line('Pickup:', Utils.formatDate(booking.pickupDate));
    line('Return:', Utils.formatDate(booking.returnDate));
    line('From:', booking.pickupAddress);
    line('To:', booking.deliveryAddress);
    if (booking.cargoDescription) line('Cargo:', booking.cargoDescription);
    if (booking.weight) line('Weight:', `${booking.weight} kg`);
    if (booking.specialInstructions) line('Notes:', booking.specialInstructions);
    line('Status:', Utils.formatStatus(booking.status));
    if (booking.totalAmount) line('Estimated Total:', `R ${Number(booking.totalAmount).toLocaleString('en-ZA', { minimumFractionDigits: 2 })}`);

    y += 6;
    doc.setFontSize(9);
    doc.setTextColor(100, 100, 100);
    doc.text(`Generated ${new Date().toLocaleString('en-ZA')} — ${name}`, 20, y);

    doc.save(`${booking.bookingReference}.pdf`);
    Utils.toast('PDF downloaded', 'success');
  }
};
