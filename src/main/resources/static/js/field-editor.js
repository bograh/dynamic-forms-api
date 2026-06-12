(() => {
  const TYPES_WITH_OPTIONS = ['SELECT', 'RADIO', 'CHECKBOX'];
  const fieldTypes = JSON.parse(document.getElementById('field-types-json').textContent);
  const initialFields = JSON.parse(document.getElementById('initial-fields-json').textContent);
  const container = document.getElementById('fields-container');
  const fieldTemplate = document.getElementById('field-row-template');
  const optionTemplate = document.getElementById('option-row-template');
  let dragSrc = null;

  function getCsrf() {
    return {
      token: document.getElementById('_csrf').content,
      header: document.getElementById('_csrf_header').content,
    };
  }

  function showToast(message, isError) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `fixed top-4 right-4 px-5 py-3 rounded-lg shadow-lg text-sm font-medium z-50 ${
      isError ? 'bg-red-600 text-white' : 'bg-green-600 text-white'
    }`;
    setTimeout(() => toast.classList.add('hidden'), 3000);
  }

  function buildTypeOptions(select, selectedType) {
    fieldTypes.forEach(t => {
      const opt = document.createElement('option');
      opt.value = t;
      opt.textContent = t;
      if (t === selectedType) opt.selected = true;
      select.appendChild(opt);
    });
  }

  function toggleOptionsSection(row, type) {
    const section = row.querySelector('.options-section');
    section.classList.toggle('hidden', !TYPES_WITH_OPTIONS.includes(type));
  }

  function addOptionRow(optionsList, label, value) {
    const clone = optionTemplate.content.cloneNode(true);
    const div = clone.querySelector('.option-row');
    if (label !== undefined) div.querySelector('[name=optLabel]').value = label;
    if (value !== undefined) div.querySelector('[name=optValue]').value = value;
    div.querySelector('.remove-option-btn').addEventListener('click', () => div.remove());
    optionsList.appendChild(div);
  }

  function createFieldRow(field) {
    const clone = fieldTemplate.content.cloneNode(true);
    const row = clone.querySelector('.field-row');

    const labelInput = row.querySelector('[name=label]');
    const keyInput = row.querySelector('[name=fieldKey]');
    const typeSelect = row.querySelector('[name=fieldType]');
    const placeholderInput = row.querySelector('[name=placeholder]');
    const helpInput = row.querySelector('[name=helpText]');
    const requiredCheck = row.querySelector('[name=required]');
    const optionsList = row.querySelector('.options-list');
    const addOptionBtn = row.querySelector('.add-option-btn');

    buildTypeOptions(typeSelect, field?.fieldType ?? 'TEXT');
    if (field) {
      labelInput.value = field.label ?? '';
      keyInput.value = field.fieldKey ?? '';
      placeholderInput.value = field.placeholder ?? '';
      helpInput.value = field.helpText ?? '';
      requiredCheck.checked = field.required ?? false;
      (field.options ?? []).forEach(o => addOptionRow(optionsList, o.label, o.value));
    }
    toggleOptionsSection(row, typeSelect.value);

    typeSelect.addEventListener('change', () => {
      toggleOptionsSection(row, typeSelect.value);
    });
    addOptionBtn.addEventListener('click', () => addOptionRow(optionsList, '', ''));
    row.querySelector('.remove-field-btn').addEventListener('click', () => row.remove());

    row.addEventListener('dragstart', e => { dragSrc = row; e.dataTransfer.effectAllowed = 'move'; });
    row.addEventListener('dragover', e => { e.preventDefault(); e.dataTransfer.dropEffect = 'move'; });
    row.addEventListener('drop', e => {
      e.preventDefault();
      if (dragSrc !== row) {
        const rows = [...container.querySelectorAll('.field-row')];
        const srcIdx = rows.indexOf(dragSrc);
        const tgtIdx = rows.indexOf(row);
        if (srcIdx < tgtIdx) row.after(dragSrc);
        else row.before(dragSrc);
      }
    });

    return row;
  }

  function collectFields() {
    return [...container.querySelectorAll('.field-row')].map((row, idx) => {
      const typeSelect = row.querySelector('[name=fieldType]');
      const type = typeSelect.value;
      const options = TYPES_WITH_OPTIONS.includes(type)
        ? [...row.querySelectorAll('.option-row')].map(r => ({
            label: r.querySelector('[name=optLabel]').value,
            value: r.querySelector('[name=optValue]').value,
          }))
        : null;
      return {
        label: row.querySelector('[name=label]').value,
        fieldKey: row.querySelector('[name=fieldKey]').value,
        fieldType: type,
        placeholder: row.querySelector('[name=placeholder]').value,
        helpText: row.querySelector('[name=helpText]').value,
        required: row.querySelector('[name=required]').checked,
        fieldOrder: idx,
        defaultValue: null,
        options,
        validation: null,
      };
    });
  }

  initialFields.forEach(f => container.appendChild(createFieldRow(f)));

  document.getElementById('add-field-btn').addEventListener('click', () => {
    container.appendChild(createFieldRow(null));
  });

  document.getElementById('save-btn').addEventListener('click', async () => {
    const fields = collectFields();
    const csrf = getCsrf();
    try {
      const res = await fetch(`/admin/forms/${FORM_ID}/fields`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          [csrf.header]: csrf.token,
        },
        body: JSON.stringify(fields),
      });
      if (res.ok) {
        showToast('Fields saved successfully', false);
      } else {
        showToast('Save failed — check your inputs', true);
      }
    } catch {
      showToast('Network error — please try again', true);
    }
  });
})();
